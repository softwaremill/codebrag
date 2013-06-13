package com.softwaremill.codebrag.service.github

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfter, FlatSpec}
import com.softwaremill.codebrag.dao.{ObjectIdTestUtils, CommitInfoDAO, CommitReviewTaskDAO, UserDAO}
import akka.testkit.TestActorRef
import com.softwaremill.codebrag.domain.{User, CommitReviewTask, UpdatedCommit, CommitsUpdatedEvent}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.builder.{CommitInfoAssembler, UserAssembler}
import org.scalatest.mock.MockitoSugar
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.mockito.Matchers._
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import pl.softwaremill.common.util.time.FixtureTimeClock
import org.joda.time.{Interval, DateTime}
import com.softwaremill.codebrag.dao.events.NewUserRegistered

class CommitReviewTaskGeneratorSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter with MockitoSugar {

  behavior of "CommitReviewTaskGenerator"

  implicit var system = ActorSystem("MyActorSystem", ConfigFactory.load("test"))
  var generator: TestActorRef[CommitReviewTaskGenerator] = _
  var userDaoMock: UserDAO = _
  var reviewTaskDaoMock: CommitReviewTaskDAO = _
  var commitInfoDaoMock: CommitInfoDAO = _
  val FixtureTime = new DateTime(23333333)
  var fixtureClock: FixtureTimeClock = new FixtureTimeClock(FixtureTime.getMillis)

  before {
    userDaoMock = mock[UserDAO]
    reviewTaskDaoMock = mock[CommitReviewTaskDAO]
    commitInfoDaoMock = mock[CommitInfoDAO]
    generator = TestActorRef(new CommitReviewTaskGenerator(userDaoMock, reviewTaskDaoMock, commitInfoDaoMock, fixtureClock))
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

  it should "generate tasks for newly registered user, skipping commits performed by himself" in {
    // given
    val commitBySofokles = CommitInfoAssembler.randomCommit.withAuthorName("Sofokles Smart").get
    val commits = commitBySofokles :: CommitInfoAssembler.randomCommits(count = 2)
    given(commitInfoDaoMock.findForTimeRange(new Interval(FixtureTime.minusDays(7), FixtureTime))).willReturn(commits)
    val sofoklesId = ObjectIdTestUtils.oid(1)

    // when
    generator ! NewUserRegistered(sofoklesId, "login", "Sofokles Smart", "sofokles@sml.com")

    // then
    verify(reviewTaskDaoMock).save(CommitReviewTask(commits(1).id, sofoklesId))
    verify(reviewTaskDaoMock).save(CommitReviewTask(commits(2).id, sofoklesId))
    verifyNoMoreInteractions(reviewTaskDaoMock)
  }

  private def user(name: String) = {
    UserAssembler.randomUser.withFullName(name).get
  }

  private def randomCommits(count: Int) = {
    List.fill(count)(UpdatedCommit(new ObjectId(), "Author Name"))
  }

}
