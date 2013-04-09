package com.softwaremill.codebrag.service.comments

import command.AddComment
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.FakeIdGenerator
import com.softwaremill.codebrag.dao.CommitCommentDAO
import pl.softwaremill.common.util.time.FixtureTimeClock
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain._
import org.joda.time.{DateTimeZone, DateTime}
import org.bson.types.ObjectId

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
  val FixtureComment = new CommitComment(FixtureCommentId, FixtureCommitId, FixtureAuthorId, "commentMsg", currentTimeUTC)

  val FixtureNewComment = AddComment(FixtureCommitId, FixtureAuthorId, "new comment message")

  var commentService: CommentService = _
  var commentDaoMock: CommitCommentDAO = _

  override def beforeEach() {
    commentDaoMock = mock[CommitCommentDAO]
    commentService = new CommentService(commentDaoMock)
  }

  it should "create a new commit and call dao to persist it" in {
    val comment = CommitComment(FixtureGeneratedId, FixtureCommitId, FixtureAuthorId, "new comment message", currentTimeUTC)

    // when
    commentService.addCommentToCommit(FixtureNewComment)

    // then
    verify(commentDaoMock).save(comment)
  }

  it should "return new comment as a result" in {

    // when
    val newCommentItem = commentService.addCommentToCommit(FixtureNewComment)

    // then
    newCommentItem should equal(CommitComment(FixtureGeneratedId, FixtureCommitId, FixtureAuthorId, "new comment message", currentTimeUTC))
  }

  val currentTimeUTC = new DateTime(FixtureTime, DateTimeZone.UTC)
}
