package com.softwaremill.codebrag.service.commits

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfter}
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import org.mockito.BDDMockito._
import com.softwaremill.codebrag.dao.{ObjectIdTestUtils}
import org.mockito.Mockito._
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.domain.{CommitInfo, CommitReviewTask}
import com.softwaremill.codebrag.common.ClockSpec
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.reviewtask.CommitReviewTaskDAO
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO

class CommitReviewTaskGeneratorActionsSpec
  extends FlatSpec with ShouldMatchers with BeforeAndAfter with MockitoSugar with ClockSpec {

  behavior of "CommitReviewTaskGeneratorActions"
  val FixtureCommitsToFetch = CommitReviewTaskGeneratorActions.LastCommitsToReviewCount
  var userDaoMock: UserDAO = _
  var reviewTaskDaoMock: CommitReviewTaskDAO = _
  var commitInfoDaoMock: CommitInfoDAO = _
  var repoStatusDaoMock: RepositoryStatusDAO = _
  var generator: CommitReviewTaskGeneratorActions = _

  before {
    userDaoMock = mock[UserDAO]
    reviewTaskDaoMock = mock[CommitReviewTaskDAO]
    commitInfoDaoMock = mock[CommitInfoDAO]
    repoStatusDaoMock = mock[RepositoryStatusDAO]
    generator = new {
      val userDao = userDaoMock
      val commitToReviewDao = reviewTaskDaoMock
      val commitInfoDao = commitInfoDaoMock
      val repoStatusDao = repoStatusDaoMock
    } with CommitReviewTaskGeneratorActions
  }

  it should "generate tasks for newly registered user, skipping commits performed by himself, when the author name matches" in {
    testGenerateTasksForNewlyRegisteredUser(CommitInfoAssembler.randomCommit.withAuthorName("Sofokles Smart").get)
  }

  it should "generate tasks for newly registered user, skipping commits performed by himself, when the author email matches" in {
    testGenerateTasksForNewlyRegisteredUser(CommitInfoAssembler.randomCommit.withAuthorEmail("sofokles@sml.com").get)
  }

  def testGenerateTasksForNewlyRegisteredUser(commitBySofokles: CommitInfo) {
    // given
    val commits = CommitInfoAssembler.randomCommits(count = 2)
    val sofoklesId = ObjectIdTestUtils.oid(1)
    val user = NewUserRegistered(sofoklesId, "login", "Sofokles Smart", "sofokles@sml.com")
    given(commitInfoDaoMock.findLastCommitsNotAuthoredByUser(user, 10)).willReturn(commits)

    // when
    generator.handleNewUserRegistered(user)

    // then
    verify(reviewTaskDaoMock).save(CommitReviewTask(commits(0).id, sofoklesId))
    verify(reviewTaskDaoMock).save(CommitReviewTask(commits(1).id, sofoklesId))
    verifyNoMoreInteractions(reviewTaskDaoMock)
  }

  it should "not generate any tasks if no commits for current user found within range" in {
    // given
    val sofoklesId = ObjectIdTestUtils.oid(1)
    val user = NewUserRegistered(sofoklesId, "login", "Sofokles Smart", "sofokles@sml.com")
    given(commitInfoDaoMock.findLastCommitsNotAuthoredByUser(user, 10)).willReturn(List.empty)

    // when
    generator.handleNewUserRegistered(user)

    // then
    verifyZeroInteractions(reviewTaskDaoMock)
  }
}
