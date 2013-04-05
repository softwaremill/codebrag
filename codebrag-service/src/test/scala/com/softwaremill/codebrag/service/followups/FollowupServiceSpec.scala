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

  var followupDAO: FollowupDAO = _
  var commitInfoDAO: CommitInfoDAO = _
  var commitReviewDAO: CommitReviewDAO = _
  var userDAO: UserDAO = _

  var followupsService: FollowupService = _

  override def beforeEach() {
    followupDAO = mock[FollowupDAO]
    commitInfoDAO = mock[CommitInfoDAO]
    commitReviewDAO = mock[CommitReviewDAO]
    userDAO = mock[UserDAO]
    followupsService = new FollowupService(followupDAO, commitInfoDAO, commitReviewDAO, userDAO)(TestClock)
  }

  it should "generate follow-ups for commit for commit author and all commenters except of current commenter" in {
    // Given
    given(commitInfoDAO.findByCommitId(Commit.id)).willReturn(Some(Commit))
    given(commitReviewDAO.findById(Commit.id)).willReturn(Some(CommitReviewWithTwoDifferentCommenters))
    given(userDAO.findByUserName(CommitAuthor.name)).willReturn(Some(CommitAuthor))

    // When
    followupsService.generateFollowupsForComment(AddCommentByUserOne)

    // Then
    verify(followupDAO).createOrUpdateExisting(Followup(Commit, UserTwoId, FollowupCreationDateTime))
    verify(followupDAO).createOrUpdateExisting(Followup(Commit, CommitAuthorId, FollowupCreationDateTime))
    verifyNoMoreInteractions(followupDAO)
  }

  it should "generate follow-ups for each user only once" in {
    // Given
    given(commitInfoDAO.findByCommitId(Commit.id)).willReturn(Some(Commit))
    given(commitReviewDAO.findById(Commit.id)).willReturn(Some(CommitReviewWithNonUniqueCommenters))
    given(userDAO.findByUserName(CommitAuthor.name)).willReturn(Some(CommitAuthor))

    // When
    followupsService.generateFollowupsForComment(AddCommentByUserOne)

    // Then
    verify(followupDAO).createOrUpdateExisting(Followup(Commit, UserTwoId, FollowupCreationDateTime))
    verify(followupDAO).createOrUpdateExisting(Followup(Commit, CommitAuthorId, FollowupCreationDateTime))
    verifyNoMoreInteractions(followupDAO);
  }

  it should "throw exception and not generate follow-ups when commit not found" in {
    // Given
    given(commitInfoDAO.findByCommitId(Commit.id)).willReturn(None)
    given(commitReviewDAO.findById(Commit.id)).willReturn(Some(CommitReviewWithTwoDifferentCommenters))

    // When
    val thrown = intercept[RuntimeException] {
      followupsService.generateFollowupsForComment(AddCommentByUserOne)
    }
    thrown.getMessage should be(s"Commit ${Commit.id} not found. Cannot createOrUpdateExisting follow-ups for nonexisting commit")
    verifyZeroInteractions(followupDAO)
  }

  it should "throw exception and not generate follow-ups for comments when no comments found" in {
    // Given
    given(commitInfoDAO.findByCommitId(Commit.id)).willReturn(Some(Commit))
    given(commitReviewDAO.findById(Commit.id)).willReturn(None)

    // When
    val thrown = intercept[RuntimeException] {
      followupsService.generateFollowupsForComment(AddCommentByUserOne)
    }
    thrown.getMessage should be(s"Commit review for commit ${Commit.id} not found. Cannot createOrUpdateExisting follow-ups for commit without comments")
    verifyZeroInteractions(followupDAO)
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

  val UserOneComment = CommitComment(new ObjectId(), UserOneId, "user one comment", CommentDateTime)
  val UserTwoComment = CommitComment(new ObjectId(), UserTwoId, "user two comment", CommentDateTime)
  val UserTwoAnotherComment = CommitComment(new ObjectId(), UserTwoId, "user two another comment", CommentDateTime)

  val AddCommentByUserOne = AddComment(Commit.id, UserOneId, UserOneComment.message)

  val CommitReviewWithTwoDifferentCommenters = CommitReview(Commit.id, List(UserOneComment, UserTwoComment))
  val CommitReviewWithNonUniqueCommenters = CommitReview(Commit.id, List(UserOneComment, UserTwoComment, UserTwoAnotherComment))

}

