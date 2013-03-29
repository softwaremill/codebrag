package com.softwaremill.codebrag.service.comments

import command.AddComment
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.FakeIdGenerator
import com.softwaremill.codebrag.dao.{CommitReviewDAO, UserDAO}
import pl.softwaremill.common.util.time.FixtureTimeClock
import org.mockito.BDDMockito._
import org.mockito.Matchers._
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain._
import org.joda.time.{DateTimeZone, DateTime}
import org.bson.types.ObjectId
import scala.Some
import com.softwaremill.codebrag.dao.reporting.CommentListItemDTO
import java.util.Date

class CommentServiceSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach {

  behavior of "CommentService"
  val FixtureCommentId = new ObjectId("507f191e810c19729de860ea")
  val FixtureCommitId = new ObjectId("507f191e810c19729de860eb")
  val FixtureGeneratedId = new ObjectId("507f1f77bcf86cd799439011")
  val FixtureAuthorId = new ObjectId("507f1f77bcf86cd799439012")
  implicit val IdGenerator = new FakeIdGenerator(FixtureGeneratedId.toString)
  val FixtureTime = 123456789
  implicit val Clock = new FixtureTimeClock(FixtureTime)
  val authentication: Authentication = new Authentication("github", "fixture-user-login", "fixture-user-login", "token", "salt")
  val FixtureUser = new User(FixtureAuthorId, authentication, "Bob", "bob@sml.com", "token")
  val FixtureComment = new CommitComment(FixtureCommentId, FixtureAuthorId, "commentMsg", currentTimeUTC())
  val FixtureReview = CommitReview(FixtureCommitId, List(FixtureComment))

  var commentService: CommentService = _
  var reviewDaoMock: CommitReviewDAO = _
  var userDaoMock: UserDAO = _

  override def beforeEach() {
    reviewDaoMock = mock[CommitReviewDAO]
    userDaoMock = mock[UserDAO]
    given(userDaoMock.findByLoginOrEmail("fixture-user-login")).willReturn(Some(FixtureUser))
    commentService = new CommentService(reviewDaoMock, userDaoMock)
  }

  it should "create a new review with new commit and persist this review" in {
    given(reviewDaoMock.findById(FixtureCommitId)) willReturn None
    val command = AddComment(FixtureCommitId, "fixture-user-login", "new comment message")
    val comment = CommitComment(FixtureGeneratedId, FixtureAuthorId, "new comment message", currentTimeUTC())
    val commitWithOneComment = CommitReview(FixtureCommitId, List(comment))

    // when
    commentService.addCommentToCommit(command)

    // then
    verify(reviewDaoMock).save(commitWithOneComment)
  }

  it should "add new comment to a review and call dao to persist this review" in {

    given(reviewDaoMock.findById(FixtureCommitId)) willReturn (Some(FixtureReview))
    val command = AddComment(FixtureCommitId, "fixture-user-login", "new comment message")

    // when
    commentService.addCommentToCommit(command)

    // then
    verify(reviewDaoMock).findById(FixtureCommitId)
    verify(userDaoMock).findByLoginOrEmail("fixture-user-login")
    verify(reviewDaoMock).save(any[CommitReview])
  }

  it should "return new comment list item as a result" in {

    given(reviewDaoMock.findById(FixtureCommitId)) willReturn (Some(FixtureReview))
    val command = AddComment(FixtureCommitId, "fixture-user-login", "new comment message")

    // when
    val newCommentItem = commentService.addCommentToCommit(command)

    // then
    newCommentItem should equal(CommentListItemDTO(FixtureGeneratedId.toString, "Bob", "new comment message", new Date(FixtureTime)))
  }

  def currentTimeUTC() = new DateTime(FixtureTime, DateTimeZone.UTC)
}
