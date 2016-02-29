package com.softwaremill.codebrag.usecases.reactions

import com.softwaremill.codebrag.cache.{BranchCommitCacheEntry, RepositoriesCache, UserReviewedCommitsCache}
import com.softwaremill.codebrag.common.{ClockSpec, EventBus}
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.{User, PartialCommitInfo, ReviewedCommit}
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import com.softwaremill.codebrag.domain.reactions.AllCommitsReviewedEvent
import com.softwaremill.codebrag.finders.browsingcontext.UserBrowsingContext
import org.bson.types.ObjectId
import org.mockito.Mockito._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, FlatSpec}

class ReviewAllCommitsUseCaseSpec
  extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach with ClockSpec {

  var eventBus: EventBus = _
  var userDAO: UserDAO = _
  var commitInfoDao: CommitInfoDAO = _
  var repositoriesCache: RepositoriesCache = _
  var reviewedCommitsCache: UserReviewedCommitsCache = _

  val userId = ObjectId.get
  var user: User = _

  var useCase: ReviewAllCommitsUseCase = _

  val RepoName = "codebrag"
  val BranchName = "master"

  val context = UserBrowsingContext(userId, RepoName, BranchName, Set())

  val commit = CommitInfoAssembler.randomCommit.withRepo(RepoName).get
  val commitCache = BranchCommitCacheEntry(commit.sha, commit.authorName, commit.authorEmail, commit.authorDate)

  override def beforeEach() {
    eventBus = mock[EventBus]
    userDAO = mock[UserDAO]
    commitInfoDao = mock[CommitInfoDAO]
    repositoriesCache = mock[RepositoriesCache]
    reviewedCommitsCache = mock[UserReviewedCommitsCache]

    user = mock[User]
    when(user.id).thenReturn(userId)

    useCase = new ReviewAllCommitsUseCase(userDAO, eventBus, commitInfoDao, reviewedCommitsCache)

    // given
    when(userDAO.findById(userId)).thenReturn(Option(user))
    when(repositoriesCache.getBranchCommits(RepoName, BranchName)).thenReturn(List(commitCache))
    when(commitInfoDao.findByShaList(RepoName, List(commit.sha))).thenReturn(List(PartialCommitInfo(commit)))
  }

  it should "generate all commits reviewed event" in {
    // when
    useCase.execute(context, List(commit.sha))

    // then
    verify(eventBus).publish(AllCommitsReviewedEvent(RepoName, BranchName, user.id))
  }

  it should "mark all commits as reviewed if commits were found" in {
    // when
    useCase.execute(context, List(commit.sha))

    // then
    val expectedCommitReviewed = ReviewedCommit(commit.sha, user.id, RepoName, clock.nowUtc)
    verify(reviewedCommitsCache).markCommitAsReviewed(expectedCommitReviewed)
  }
}
