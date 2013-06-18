package com.softwaremill.codebrag.service.github

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfter}
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import org.mockito.BDDMockito._
import org.joda.time.{DateTime, Interval}
import com.softwaremill.codebrag.dao.{CommitInfoDAO, CommitReviewTaskDAO, UserDAO, ObjectIdTestUtils}
import org.mockito.Mockito._
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.domain.CommitReviewTask
import pl.softwaremill.common.util.time.FixtureTimeClock

class CommitReviewTaskGeneratorActionsSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter with MockitoSugar {

  behavior of "CommitReviewTaskGeneratorActions"
  val FixtureDaysToFetch = CommitReviewTaskGeneratorActions.LastDaysToFetchCount
  var userDaoMock: UserDAO = _
  var reviewTaskDaoMock: CommitReviewTaskDAO = _
  var commitInfoDaoMock: CommitInfoDAO = _
  val FixtureTime = new DateTime(23333333)
  var fixtureClock: FixtureTimeClock = new FixtureTimeClock(FixtureTime.getMillis)
  var generator: CommitReviewTaskGeneratorActions = _

  before {
    userDaoMock = mock[UserDAO]
    reviewTaskDaoMock = mock[CommitReviewTaskDAO]
    commitInfoDaoMock = mock[CommitInfoDAO]
    generator = new {
      val userDao = userDaoMock
      val commitToReviewDao = reviewTaskDaoMock
      val commitInfoDao = commitInfoDaoMock
      val clock = fixtureClock
    } with CommitReviewTaskGeneratorActions
  }

  it should "generate tasks for newly registered user, skipping commits performed by himself" in {
    // given
    val commitBySofokles = CommitInfoAssembler.randomCommit.withAuthorName("Sofokles Smart").get
    val commits = commitBySofokles :: CommitInfoAssembler.randomCommits(count = 2)
    given(commitInfoDaoMock.findForTimeRange(new Interval(FixtureTime.minusDays(FixtureDaysToFetch), FixtureTime))).willReturn(commits)
    val sofoklesId = ObjectIdTestUtils.oid(1)

    // when
    generator.handleNewUserRegistered(NewUserRegistered(sofoklesId, "login", "Sofokles Smart", "sofokles@sml.com"))

    // then
    verify(reviewTaskDaoMock).save(CommitReviewTask(commits(1).id, sofoklesId))
    verify(reviewTaskDaoMock).save(CommitReviewTask(commits(2).id, sofoklesId))
    verifyNoMoreInteractions(reviewTaskDaoMock)
  }


  it should "not generate any tasks if no commits for current user found within range" in {
    // given
    val commit = List(CommitInfoAssembler.randomCommit.withAuthorName("Sofokles Smart").get)
    given(commitInfoDaoMock.findForTimeRange(new Interval(FixtureTime.minusDays(FixtureDaysToFetch), FixtureTime))).willReturn(commit)
    val sofoklesId = ObjectIdTestUtils.oid(1)

    // when
    generator.handleNewUserRegistered(NewUserRegistered(sofoklesId, "login", "Sofokles Smart", "sofokles@sml.com"))

    // then
    verifyZeroInteractions(reviewTaskDaoMock)
  }
}
