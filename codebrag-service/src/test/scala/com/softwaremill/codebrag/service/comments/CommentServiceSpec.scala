package com.softwaremill.codebrag.service.comments

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.FakeIdGenerator
import com.softwaremill.codebrag.dao.{UserDAO, CommitInfoDAO}
import pl.softwaremill.common.util.time.FixtureTimeClock
import org.mockito.BDDMockito._
import org.mockito.Matchers._
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.{Authentication, User, CommitComment, CommitInfo}
import org.joda.time.DateTime
import org.bson.types.ObjectId

class CommentServiceSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach {

  behavior of "CommentService"
  val FixtureCommentId = new ObjectId("507f191e810c19729de860ea")
  val FixtureCommitId = new ObjectId("507f191e810c19729de860eb")
  val NewCommentId = new ObjectId("507f1f77bcf86cd799439011")
  implicit val IdGenerator = new FakeIdGenerator(NewCommentId.toString)
  val FixtureTime = 123456789
  implicit val Clock = new FixtureTimeClock(FixtureTime)
  val authentication: Authentication = new Authentication("github", "fixture-user-login", "fixture-user-login", "token", "salt")
  val FixtureUser = new User(new ObjectId("507f1f77bcf86cd799439011"), authentication,
    "Bob", "bob@sml.com", "token")
  val FixtureComment = new CommitComment(FixtureCommentId, "commentAuthor", "commentMsg", new DateTime(FixtureTime))
  val FixtureCommit = new CommitInfo(FixtureCommitId, "fixture-commit-sha", "msg", "authorName", "committerName",
    new DateTime(FixtureTime), List.empty, List(FixtureComment))

  var commentService: CommentService = _
  var commitDaoMock: CommitInfoDAO = _
  var userDaoMock: UserDAO = _

  override def beforeEach() {
    commitDaoMock = mock[CommitInfoDAO]
    userDaoMock = mock[UserDAO]
    commentService = new CommentService(commitDaoMock, userDaoMock)
  }

  it should "add new comment to a commit and call dao to persist this commit" in {

    given(commitDaoMock.findBySha("fixture-commit-id")) willReturn (Some(FixtureCommit))
    given(userDaoMock.findByLoginOrEmail("fixture-user-login")).willReturn(Some(FixtureUser))
    val command = AddCommentCommand("fixture-commit-id", "fixture-user-login", "new comment message")

    // when
    commentService.addCommentToCommit(command)

    // then
    verify(commitDaoMock).findBySha("fixture-commit-id")
    verify(userDaoMock).findByLoginOrEmail("fixture-user-login")
    verify(commitDaoMock).storeCommit(any[CommitInfo])
  }

  it should "return new comment identifier as a result" in {

    given(commitDaoMock.findBySha("fixture-commit-id")) willReturn (Some(FixtureCommit))
    given(userDaoMock.findByLoginOrEmail("fixture-user-login")).willReturn(Some(FixtureUser))
    val command = AddCommentCommand("fixture-commit-id", "fixture-user-login", "new comment message")

    // when
    val newCommentId = commentService.addCommentToCommit(command)

    // then
    newCommentId should equal(NewCommentId)
  }

  it should "throw exception when cannot find commit" in {

    given(commitDaoMock.findBySha(any[String])) willReturn (None)
    val command = AddCommentCommand("fixture-commit-id", "fixture-user-login", "new comment message")

    // when
    val thrown = intercept[IllegalArgumentException] {
      commentService.addCommentToCommit(command)
    }
    // then
    thrown.getMessage should equal ("Cannot load commit with id = fixture-commit-id")
  }

}
