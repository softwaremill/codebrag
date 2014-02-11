package com.softwaremill.codebrag.service.followups

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao._
import org.mockito.ArgumentCaptor
import com.softwaremill.codebrag.domain._
import com.softwaremill.codebrag.domain.builder.{CommentAssembler, CommitInfoAssembler, UserAssembler, LikeAssembler}
import com.softwaremill.codebrag.domain.reactions.LikeEvent
import com.softwaremill.codebrag.domain.Followup
import scala.Some
import com.softwaremill.codebrag.domain.FollowupWithNoReactions
import com.softwaremill.codebrag.domain.reactions.UnlikeEvent
import com.softwaremill.codebrag.common.ClockSpec
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO

class FollowupsGeneratorActionsSpec
  extends FlatSpec with ShouldMatchers with BeforeAndAfterEach with MockitoSugar with ClockSpec {

  var generator: FollowupsGeneratorActions = _
  var followupDaoMock: FollowupDAO = _
  var userDaoMock: UserDAO = _
  var commitDaoMock: CommitInfoDAO = _
  var followupWithReactionsDaoMock: FollowupWithReactionsDAO = _

  val bob = UserAssembler.randomUser.withEmail("bob@smith.com").get
  val john = UserAssembler.randomUser.withEmail("john@doe.com").get
  val bobsCommit = CommitInfoAssembler.randomCommit.withAuthorEmail(bob.emailLowerCase).withAuthorName(bob.name).get
  val johnsLike = LikeAssembler.likeFor(bobsCommit.id).withAuthorId(john.id).get
  val johnsLikeEvent = LikeEvent(johnsLike)

  override def beforeEach() {
    followupDaoMock = mock[FollowupDAO]
    userDaoMock = mock[UserDAO]
    commitDaoMock = mock[CommitInfoDAO]
    followupWithReactionsDaoMock = mock[FollowupWithReactionsDAO]

    generator = new FollowupsGeneratorActions {
      override def followupDao = followupDaoMock
      override def userDao = userDaoMock
      override def commitDao = commitDaoMock
      override def followupWithReactionsDao = followupWithReactionsDaoMock
    }
  }

  it should "generate a followup for author of liked commit" in {
    // given
    when(commitDaoMock.findByCommitId(bobsCommit.id)).thenReturn(Some(bobsCommit))
    when(userDaoMock.findCommitAuthor(bobsCommit)).thenReturn(Some(bob))

    // when
    generator.handleCommitLiked(johnsLikeEvent)

    // then
    val followupArgument = ArgumentCaptor.forClass(classOf[Followup])
    verify(followupDaoMock).createOrUpdateExisting(followupArgument.capture())
    val resultFollowup = followupArgument.getValue
    resultFollowup.receivingUserId should equal(bob.id)
    resultFollowup.reaction.id should equal(johnsLike.id)
    resultFollowup.reaction.commitId should equal(bobsCommit.id)
    resultFollowup.reaction.fileName should equal(johnsLike.fileName)
    resultFollowup.reaction.lineNumber should equal(johnsLike.lineNumber)
  }

  it should "not generate a follow-up if commit author doesn't exist" in {
    // given
    val nonExistingCommitAuthor = UserAssembler.randomUser.withEmail("bob@smith.com").get
    val likeAuthor = UserAssembler.randomUser.withEmail("john@doe.com").get
    val commit = CommitInfoAssembler.randomCommit.withAuthorEmail(nonExistingCommitAuthor.emailLowerCase).withAuthorName(nonExistingCommitAuthor.name).get
    val like = LikeAssembler.likeFor(commit.id).withAuthorId(likeAuthor.id).get
    val event = LikeEvent(like)
    when(commitDaoMock.findByCommitId(commit.id)).thenReturn(Some(commit))
    when(userDaoMock.findCommitAuthor(commit)).thenReturn(None)

    // when
    generator.handleCommitLiked(event)

    // then
    verifyZeroInteractions(followupDaoMock)
  }

  it should "remove followup for given thread if loaded followup has no reactions (like was unliked)" in {
    // given
    val followup = FollowupWithNoReactions(new ObjectId, new ObjectId, ThreadDetails(bobsCommit.id))
    when(followupWithReactionsDaoMock.findAllContainingReaction(johnsLike.id)).thenReturn(List(Left(followup)))

    // when
    generator.handleUnlikeEvent(UnlikeEvent(johnsLike))

    // then
    verify(followupDaoMock).delete(followup.followupId)
  }

  it should "remove followup for given thread if removed like was the only reaction" in {
    // given
    val followup = FollowupWithReactions(new ObjectId, new ObjectId, ThreadDetails(bobsCommit.id), johnsLike, List(johnsLike))
    when(followupWithReactionsDaoMock.findAllContainingReaction(johnsLike.id)).thenReturn(List(Right(followup)))

    // when
    generator.handleUnlikeEvent(UnlikeEvent(johnsLike))

    // then
    verify(followupDaoMock).delete(followup.followupId)
  }

  it should "update followup for given thread if removed like was not the only reaction" in {
    // given
    val comment = CommentAssembler.commentFor(bobsCommit.id).get
    val followup = FollowupWithReactions(new ObjectId, new ObjectId, ThreadDetails(bobsCommit.id), johnsLike, List(johnsLike, comment))
    when(followupWithReactionsDaoMock.findAllContainingReaction(johnsLike.id)).thenReturn(List(Right(followup)))

    // when
    generator.handleUnlikeEvent(UnlikeEvent(johnsLike))

    // then
    val captor = ArgumentCaptor.forClass(classOf[FollowupWithReactions])
    verify(followupWithReactionsDaoMock).update(captor.capture())
    val modifiedFollowup = captor.getValue
    modifiedFollowup.allReactions should be(List(comment))
    modifiedFollowup.lastReaction should be(comment)
    modifiedFollowup.followupId should be(followup.followupId)
  }

}
