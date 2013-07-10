package com.softwaremill.codebrag.service.followups

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.mockito.Matchers
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao._
import pl.softwaremill.common.util.time.FixtureTimeClock
import com.softwaremill.codebrag.domain._
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler

class FollowupServiceSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach with FollowupServiceSpecFixture{

  var followupDao: FollowupDAO = _
  var commitInfoDao: CommitInfoDAO = _
  var commitCommentDao: CommitCommentDAO = _
  var userDao: UserDAO = _

  var followupService: FollowupService = _

  override def beforeEach() {
    followupDao = mock[FollowupDAO]
    commitInfoDao = mock[CommitInfoDAO]
    commitCommentDao = mock[CommitCommentDAO]
    userDao = mock[UserDAO]
    followupService = new FollowupService(followupDao, commitInfoDao, commitCommentDao, userDao)(TestClock)

    given(commitInfoDao.findByCommitId(Commit.id)).willReturn(Some(Commit))
    given(commitInfoDao.findByCommitId(Commit2.id)).willReturn(Some(Commit2))

    given(userDao.findByUserNameOrEmail(Matchers.eq(BettyCommitAuthor.name), Matchers.any())).willReturn(Some(BettyCommitAuthor))
    given(userDao.findByUserNameOrEmail(Matchers.any(), Matchers.eq(JackCommitAuthor.email))).willReturn(Some(JackCommitAuthor))

    given(userDao.findById(JohnId)).willReturn(Some(JohnCommenter))
  }

  it should "generate follow-ups for commit for commit author and all commenters except of current commenter" in {
    // Given
    given(commitCommentDao.findAllCommentsInThreadWith(JohnComment)).willReturn(JohnAndMaryComments)

    // When
    followupService.generateFollowupsForComment(JohnComment)

    // Then
    verifyFollowupsCreatedFor(JohnComment, List(MaryId, BettyCommitAuthorId))
    verifyNoMoreInteractions(followupDao)
  }

  it should "generate follow-ups for commit if the commit author has only a matching e-mail" in {
    // Given
    given(commitCommentDao.findAllCommentsInThreadWith(JohnComment2)).willReturn(List(JohnComment2))

    // When
    followupService.generateFollowupsForComment(JohnComment2)

    // Then
    verifyFollowupsCreatedFor(JohnComment2, List(JackCommitAuthorId))
    verifyNoMoreInteractions(followupDao)
  }

  it should "generate follow-ups for each user only once" in {
    // Given
    given(commitCommentDao.findAllCommentsInThreadWith(JohnComment)).willReturn(JohnAndTwoMaryComments)

    // When
    followupService.generateFollowupsForComment(JohnComment)

    // Then
    verifyFollowupsCreatedFor(JohnComment, List(MaryId, BettyCommitAuthorId))
    verifyNoMoreInteractions(followupDao)
  }

  it should "generate followups for all user in thread" in {
    // Given
    given(commitCommentDao.findAllCommentsInThreadWith(JohnComment)).willReturn(JohnAndTwoMaryComments)
    given(commitCommentDao.findAllCommentsInThreadWith(JohnInlineComment)).willReturn(JohnMaryAndBobInlineComments)

    // When
    followupService.generateFollowupsForComment(JohnComment)  // should generate for mary and betty
    followupService.generateFollowupsForComment(JohnInlineComment)  // should generate for bob betty and mary

    // Then
    verifyFollowupsCreatedFor(JohnComment, List(BettyCommitAuthorId, MaryId))
    verifyFollowupsCreatedFor(JohnInlineComment, List(BobId, BettyCommitAuthorId, MaryId))
    verifyNoMoreInteractions(followupDao)
  }

  it should "throw exception and not generate follow-ups when commit not found" in {
    // Given
    given(commitInfoDao.findByCommitId(Commit.id)).willReturn(None)
    given(commitCommentDao.findCommentsForCommit(Commit.id)).willReturn(JohnAndTwoMaryComments)

    // When
    val thrown = intercept[RuntimeException] {
      followupService.generateFollowupsForComment(JohnComment)
    }
    thrown.getMessage should be(s"Commit ${Commit.id} not found. Cannot createOrUpdateExisting follow-ups for nonexisting commit")
    verifyZeroInteractions(followupDao)
  }

  it should "not generate follow-up for commit author if he does not exist in system" in {
    // Given
    given(commitCommentDao.findAllCommentsInThreadWith(JohnComment)).willReturn(JohnAndMaryComments)
    given(userDao.findByUserNameOrEmail(Commit.authorName, Commit.authorEmail)).willReturn(None)

    // When
    followupService.generateFollowupsForComment(JohnComment)
    verifyFollowupsCreatedFor(JohnComment, List(MaryId))
    verifyNoMoreInteractions(followupDao)
  }

  it should "throw exception and not generate follow-ups for comments when no comments found" in {
    // Given
    given(commitCommentDao.findAllCommentsInThreadWith(JohnComment)).willReturn(List.empty)

    // When
    val thrown = intercept[RuntimeException] {
      followupService.generateFollowupsForComment(JohnComment)
    }
    thrown.getMessage should be(s"No stored comments for commit ${Commit.id}. Cannot createOrUpdateExisting follow-ups for commit without comments")
    verifyZeroInteractions(followupDao)
  }

  private def verifyFollowupsCreatedFor(reaction: UserReaction, users: List[ObjectId]) {
    users.foreach{ userId =>
      val followup = NewFollowup(userId, reaction)
      verify(followupDao).createOrUpdateExisting(followup)
    }
  }

}

trait FollowupServiceSpecFixture {

  val CommentDateTime = new DateTime()

  implicit val TestClock = new FixtureTimeClock(12345)
  val FollowupCreationDateTime = TestClock.currentDateTimeUTC()

  val BettyCommitAuthorId = ObjectIdTestUtils.oid(000)
  val JackCommitAuthorId = ObjectIdTestUtils.oid(124)

  val JohnId = ObjectIdTestUtils.oid(456)
  val MaryId = ObjectIdTestUtils.oid(789)
  val BobId = ObjectIdTestUtils.oid(123)

  val Commit = CommitInfoAssembler.randomCommit.get
  val Commit2 = CommitInfoAssembler.randomCommit.get

  val BettyCommitAuthor = User(BettyCommitAuthorId, Authentication.basic("user", "password"), Commit.authorName, "user@email.com", "123213", "avatarUrl")
  val JackCommitAuthor = User(JackCommitAuthorId, Authentication.basic("jack", "doe"), "?", Commit2.authorEmail, "456456", "avatarUrl")

  val JohnCommenter = User(JohnId, Authentication.basic("john", "doe"), "John", "john@doe.com", "456456", "avatarUrl")

  val JohnComment = Comment(new ObjectId(), Commit.id, JohnId, CommentDateTime, "user one comment")
  val JohnComment2 = Comment(new ObjectId(), Commit2.id, JohnId, CommentDateTime, "user one comment")
  val MaryComment = Comment(new ObjectId(), Commit.id, MaryId, CommentDateTime, "user two comment")
  val MaryAnotherComment = Comment(new ObjectId(), Commit.id, MaryId, CommentDateTime, "user two another comment")

  val InlineCommentFile = "file_1.txt"
  val InlineCommentLine = 20
  val JohnInlineComment = Comment(new ObjectId(), Commit.id, JohnId, CommentDateTime, "user one inline comment", Some("file_1.txt"), Some(20))
  val MaryInlineComment = Comment(new ObjectId(), Commit.id, MaryId, CommentDateTime, "user two inline comment", Some("file_1.txt"), Some(20))
  val BobInlineComment = Comment(new ObjectId(), Commit.id, BobId, CommentDateTime, "user three another inline comment", Some("file_1.txt"), Some(20))

  val JohnAndMaryComments = List(JohnComment, MaryComment)
  val JohnAndTwoMaryComments = List(JohnComment, MaryComment, MaryAnotherComment)

  val JohnMaryAndBobInlineComments = List(JohnInlineComment, MaryInlineComment, BobInlineComment)
}

