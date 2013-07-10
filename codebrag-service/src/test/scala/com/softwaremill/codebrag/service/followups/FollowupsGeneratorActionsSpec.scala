package com.softwaremill.codebrag.service.followups

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.reactions.CommitLiked
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.{CommitInfoDAO, UserDAO, FollowupDAO}
import org.mockito.ArgumentCaptor
import com.softwaremill.codebrag.domain._
import org.joda.time.DateTime
import org.mockito.BDDMockito._
import com.softwaremill.codebrag.domain.reactions.CommitLiked
import scala.Some
import com.softwaremill.codebrag.domain.Like

class FollowupsGeneratorActionsSpec extends FlatSpec with ShouldMatchers with BeforeAndAfterEach with MockitoSugar {

  behavior of "FollowupsGeneratorActions"

  var generator: FollowupsGeneratorActions = _
  var followupDaoMock: FollowupDAO = _
  var userDaoMock: UserDAO = _
  var commitDaoMock: CommitInfoDAO = _

  val commitId = new ObjectId
  val likeId = new ObjectId
  val likeSenderId = new ObjectId
  val likeDate = new DateTime
  val likeAuthorName = "Like Author Name"
  val commitAuthorName = "Lazy Val"
  val likeFileName = "file.txt"
  val likeLineNumber = 27
  val like = Like(likeId, commitId, likeSenderId, likeDate, Some(likeFileName), Some(likeLineNumber))
  val event = CommitLiked(like)

  override def beforeEach() {
    followupDaoMock = mock[FollowupDAO]
    userDaoMock = mock[UserDAO]
    commitDaoMock = mock[CommitInfoDAO]

    generator = new FollowupsGeneratorActions {
      override def followupDao = followupDaoMock
      override def userDao = userDaoMock
      override def commitDao = commitDaoMock
    }
  }

  it should "generate a followup for author of liked commit" in {
    // given
    val likeAuthor = mock[User]
    val commitAuthor = mock[User]
    given(userDaoMock.findById(likeSenderId)).willReturn(Some(likeAuthor))
    given(likeAuthor.name).willReturn(likeAuthorName)
    val commitMock = mock[CommitInfo]
    given(commitDaoMock.findByCommitId(commitId)).willReturn(Some(commitMock))
    given(commitMock.authorName).willReturn(commitAuthorName)
    given(userDaoMock.findByUserName(commitAuthorName)).willReturn(Some(commitAuthor))

    // when
    generator.handleCommitLiked(event)

    // then
    val followupArgument = ArgumentCaptor.forClass(classOf[NewFollowup])

    verify(followupDaoMock).createOrUpdateExisting(followupArgument.capture())
    val resultFollowup: NewFollowup = followupArgument.getValue
    resultFollowup.reaction.id should equal(likeId)
    resultFollowup.reaction.postingTime should equal(likeDate)
    resultFollowup.reaction.commitId should equal(commitId)
    resultFollowup.reaction.fileName.get should equal(likeFileName)
    resultFollowup.reaction.lineNumber.get should equal(likeLineNumber)
  }

  it should "not generate a follow-up if commit author doesn't exist" in {
    // given
    val userMock = mock[User]
    given(userDaoMock.findById(likeSenderId)).willReturn(Some(userMock))
    given(userMock.name).willReturn(likeAuthorName)
    val commitMock = mock[CommitInfo]
    given(commitDaoMock.findByCommitId(commitId)).willReturn(Some(commitMock))
    given(commitMock.authorName).willReturn(commitAuthorName)
    given(userDaoMock.findByUserName(commitAuthorName)).willReturn(None)

    // when
    generator.handleCommitLiked(event)

    // then
    verifyZeroInteractions(followupDaoMock)
  }

}
