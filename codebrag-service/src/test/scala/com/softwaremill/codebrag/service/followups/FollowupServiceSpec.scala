package com.softwaremill.codebrag.service.followups

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao._
import pl.softwaremill.common.util.time.FixtureTimeClock
import com.softwaremill.codebrag.domain._
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.CommitComment
import com.softwaremill.codebrag.domain.Followup
import scala.Some
import com.softwaremill.codebrag.service.comments.command.AddComment

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
  }

  it should "generate follow-ups for commit for commit author and all commenters except of current commenter" in {
    // Given
    given(commitInfoDao.findByCommitId(Commit.id)).willReturn(Some(Commit))
    given(userDao.findByUserName(CommitAuthor.name)).willReturn(Some(CommitAuthor))
    given(commitCommentDao.findAllForCommit(Commit.id)).willReturn(CommentsWithTwoDifferentCommenters)

    // When
    followupService.generateFollowupsForComment(AddCommentByUserOne)

    // Then
    verify(followupDao).createOrUpdateExisting(Followup(Commit, UserTwoId, FollowupCreationDateTime))
    verify(followupDao).createOrUpdateExisting(Followup(Commit, CommitAuthorId, FollowupCreationDateTime))
    verifyNoMoreInteractions(followupDao)
  }

  it should "generate follow-ups for each user only once" in {
    // Given
    given(commitInfoDao.findByCommitId(Commit.id)).willReturn(Some(Commit))
    given(userDao.findByUserName(CommitAuthor.name)).willReturn(Some(CommitAuthor))
    given(commitCommentDao.findAllForCommit(Commit.id)).willReturn(CommentsWithNonUniqueCommenters)

    // When
    followupService.generateFollowupsForComment(AddCommentByUserOne)

    // Then
    verify(followupDao).createOrUpdateExisting(Followup(Commit, UserTwoId, FollowupCreationDateTime))
    verify(followupDao).createOrUpdateExisting(Followup(Commit, CommitAuthorId, FollowupCreationDateTime))
    verifyNoMoreInteractions(followupDao);
  }

  it should "throw exception and not generate follow-ups when commit not found" in {
    // Given
    given(commitInfoDao.findByCommitId(Commit.id)).willReturn(None)
    given(commitCommentDao.findAllForCommit(Commit.id)).willReturn(CommentsWithNonUniqueCommenters)

    // When
    val thrown = intercept[RuntimeException] {
      followupService.generateFollowupsForComment(AddCommentByUserOne)
    }
    thrown.getMessage should be(s"Commit ${Commit.id} not found. Cannot createOrUpdateExisting follow-ups for nonexisting commit")
    verifyZeroInteractions(followupDao)
  }

  it should "throw exception and not generate follow-ups for comments when no comments found" in {
    // Given
    given(commitInfoDao.findByCommitId(Commit.id)).willReturn(Some(Commit))
    given(commitCommentDao.findAllForCommit(Commit.id)).willReturn(List.empty)

    // When
    val thrown = intercept[RuntimeException] {
      followupService.generateFollowupsForComment(AddCommentByUserOne)
    }
    thrown.getMessage should be(s"No stored comments for commit ${Commit.id}. Cannot createOrUpdateExisting follow-ups for commit without comments")
    verifyZeroInteractions(followupDao)
  }

}

trait FollowupServiceSpecFixture {

  val CommentDateTime = new DateTime()

  implicit val TestClock = new FixtureTimeClock(12345)
  val FollowupCreationDateTime = TestClock.currentDateTimeUTC()

  val CommitAuthorId = ObjectIdTestUtils.oid(000)
  val UserOneId = ObjectIdTestUtils.oid(456)
  val UserTwoId = ObjectIdTestUtils.oid(789)

  val Commit = CommitInfoBuilder.createRandomCommit()

  val CommitAuthor = User(CommitAuthorId, Authentication.basic("user", "password"), Commit.authorName, "user@email.com", "123213")

  val UserOneComment = CommitComment(new ObjectId(), Commit.id, UserOneId, "user one comment", CommentDateTime)
  val UserTwoComment = CommitComment(new ObjectId(), Commit.id, UserTwoId, "user two comment", CommentDateTime)
  val UserTwoAnotherComment = CommitComment(new ObjectId(), Commit.id, UserTwoId, "user two another comment", CommentDateTime)

  val AddCommentByUserOne = AddComment(Commit.id, UserOneId, UserOneComment.message)

  val CommentsWithTwoDifferentCommenters = List(UserOneComment, UserTwoComment)
  val CommentsWithNonUniqueCommenters = List(UserOneComment, UserTwoComment, UserTwoAnotherComment)
}

