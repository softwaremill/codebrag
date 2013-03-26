package com.softwaremill.codebrag.service.comments

import org.scalatest.{BeforeAndAfterEach, GivenWhenThen, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.FakeIdGenerator
import com.softwaremill.codebrag.dao.{UserDAO, CommitInfoDAO}
import pl.softwaremill.common.util.time.FixtureTimeClock
import org.mockito.BDDMockito
import org.mockito.Matchers._
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.{Authentication, User, CommitComment, CommitInfo}
import org.joda.time.DateTime
import org.bson.types.ObjectId

class CommentServiceSpec extends FlatSpec with MockitoSugar with GivenWhenThen with ShouldMatchers with BeforeAndAfterEach {

  behavior of "CommentService"
  implicit val IdGenerator = new FakeIdGenerator("fixture-new-comment-id")
  val FixtureTime = 123456789
  implicit val Clock = new FixtureTimeClock(FixtureTime)
  val authentication: Authentication = new Authentication("github", "fixture-user-login", "fixture-user-login", "token", "salt")
  val FixtureUser = new User(new ObjectId("507f1f77bcf86cd799439011"), authentication,
    "Bob", "bob@sml.com", "token")
  val FixtureComment = new CommitComment("fixture-comment-id", "commentAuthor", "commentMsg", new DateTime(FixtureTime))
  val FixtureCommit = new CommitInfo("fixture-commit-id", "msg", "authorName", "committerName",
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
    Given("One commit in dao and fixture user")
    BDDMockito.given(commitDaoMock.findBySha("fixture-commit-id")) willReturn (Some(FixtureCommit))
    BDDMockito.given(userDaoMock.findByLoginOrEmail("fixture-user-login")).willReturn(Some(FixtureUser))
    val command = AddCommentCommand("fixture-commit-id", "fixture-user-login", "new comment message")

    When("Command is executed on service")
    commentService.addCommentToCommit(command)

    Then("User and commit dao should be queried")
    verify(commitDaoMock).findBySha("fixture-commit-id")
    verify(userDaoMock).findByLoginOrEmail("fixture-user-login")
    And("Commit dao should be called to persist new data")
    verify(commitDaoMock).storeCommit(any[CommitInfo])
  }

  it should "return new comment identifier as a result" in {
    Given("One commit in dao and fixture user")
    BDDMockito.given(commitDaoMock.findBySha("fixture-commit-id")) willReturn (Some(FixtureCommit))
    BDDMockito.given(userDaoMock.findByLoginOrEmail("fixture-user-login")).willReturn(Some(FixtureUser))
    val command = AddCommentCommand("fixture-commit-id", "fixture-user-login", "new comment message")

    When("Command is executed on service")
    val newCommentId = commentService.addCommentToCommit(command)

    Then("User and commit dao should be queried")
    newCommentId should equal("fixture-new-comment-id")
  }

  it should "throw exception when cannot find commit" in {
    Given("Commit dao returns no commits")
    BDDMockito.given(commitDaoMock.findBySha(any[String])) willReturn (None)
    val command = AddCommentCommand("fixture-commit-id", "fixture-user-login", "new comment message")

    When("Command is executed on service")
    val thrown = intercept[IllegalArgumentException] {
      commentService.addCommentToCommit(command)
    }
    Then("Exception should be thrown")
    thrown.getMessage should equal ("Cannot load commit with id = fixture-commit-id")
  }

}
