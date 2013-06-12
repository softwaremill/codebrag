package com.softwaremill.codebrag.service.github

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfter, FlatSpec}
import com.softwaremill.codebrag.dao.{CommitReviewTaskDAO, UserDAO}
import akka.testkit.TestActorRef
import com.softwaremill.codebrag.domain.{User, CommitReviewTask, UpdatedCommit, CommitsUpdatedEvent}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.builder.UserAssembler
import org.scalatest.mock.MockitoSugar
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.mockito.Matchers._
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

class CommitReviewTaskGeneratorSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter with MockitoSugar {

  behavior of "CommitReviewTaskGenerator"

  implicit var system = ActorSystem("MyActorSystem", ConfigFactory.load("test"))
  var generator: TestActorRef[CommitReviewTaskGenerator] = _
  var userDaoMock: UserDAO = _
  var reviewTaskDaoMock: CommitReviewTaskDAO = _

  before {
    userDaoMock = mock[UserDAO]
    reviewTaskDaoMock = mock[CommitReviewTaskDAO]
    generator = TestActorRef(new CommitReviewTaskGenerator(userDaoMock, reviewTaskDaoMock))
  }

  it should "generate 22 tasks when there are two non-author users and 11 new commits" in {
    // given
    val commits = randomCommits(count = 11)
    val users = List(
      user(name = "Author Name"),
      user(name = "Sofokles Smart"),
      user(name = "Bruce Angry"))
    given(userDaoMock.findAll()).willReturn(users)

    // when
    generator ! CommitsUpdatedEvent(firstTime = false, commits)

    // then
    verify(reviewTaskDaoMock, times(22)).save(any[CommitReviewTask])
  }

  it should "generate 0 tasks when the only user is commit's author" in {
    // given
    val commits = randomCommits(count = 11)
    val users = List(user(name = "Author Name"))
    given(userDaoMock.findAll()).willReturn(users)

    // when
    generator ! CommitsUpdatedEvent(firstTime = false, commits)

    // then
    verify(reviewTaskDaoMock, never()).save(any[CommitReviewTask])
  }

  it should "generate only 20 tasks for each user on first update" in {
    // given
    val commits = randomCommits(count = 31)
    val users = List(
      user(name = "Author Name"),
      user(name = "Sofokles Smart"),
      user(name = "Bruce Angry"))
    given(userDaoMock.findAll()).willReturn(users)

    // when
    generator ! CommitsUpdatedEvent(firstTime = true, commits)

    // then
    verify(reviewTaskDaoMock, times(40)).save(any[CommitReviewTask])
  }

  it should "generate tasks only for non-author" in {
    // given
    val commits = randomCommits(count = 1)
    val sofokles = user(name = "Sofokles Smart")
    val bruce  = user(name = "Bruce Angry")
    val commitAuthor = user(name = "Author Name")
    val users = List(
      commitAuthor,
      sofokles,
      bruce)
    given(userDaoMock.findAll()).willReturn(users)

    // when
    generator ! CommitsUpdatedEvent(firstTime = true, commits)

    // then
    verify(reviewTaskDaoMock).save(CommitReviewTask(commits(0).id, sofokles.id))
    verify(reviewTaskDaoMock).save(CommitReviewTask(commits(0).id, bruce.id))
    verify(reviewTaskDaoMock, never()).save(CommitReviewTask(commits(0).id, commitAuthor.id))

  }

  private def user(name: String) = {
    UserAssembler.randomUser.withFullName(name).get
  }

  private def randomCommits(count: Int) = {
    List.fill(count)(UpdatedCommit(new ObjectId(), "Author Name"))
  }
}
