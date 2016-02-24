package com.softwaremill.codebrag.usecases.reactions

import com.softwaremill.codebrag.cache.{BranchCommitCacheEntry, RepositoriesCache, UserReviewedCommitsCache}
import com.softwaremill.codebrag.common.{ClockSpec, EventBus}
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.domain.{User, PartialCommitInfo, ReviewedCommit}
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import com.softwaremill.codebrag.domain.reactions.AllCommitsReviewedEvent
import com.softwaremill.codebrag.finders.commits.toreview.ToReviewBranchCommitsFilter
import org.bson.types.ObjectId
import org.mockito.Mockito._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, FlatSpec}

class ReviewAllCommitsUseCaseSpec
  extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach with ClockSpec {

  var eventBus: EventBus = _
  var commitInfoDao: CommitInfoDAO = _
  var repositoriesCache: RepositoriesCache = _
  var toReviewBranchCommitsFilter: ToReviewBranchCommitsFilter = _
  var reviewedCommitsCache: UserReviewedCommitsCache = _

  var user: User = _

  var useCase: ReviewAllCommitsUseCase = _

  val RepoName = "codebrag"
  val BranchName = "master"

  val commit = CommitInfoAssembler.randomCommit.withRepo(RepoName).get
  val commitCache = BranchCommitCacheEntry(commit.sha, commit.authorName, commit.authorEmail, commit.authorDate)

  override def beforeEach() {
    eventBus = mock[EventBus]
    commitInfoDao = mock[CommitInfoDAO]
    repositoriesCache = mock[RepositoriesCache]
    toReviewBranchCommitsFilter = mock[ToReviewBranchCommitsFilter]
    reviewedCommitsCache = mock[UserReviewedCommitsCache]

    val userId = ObjectId.get
    user = mock[User]
    when(user.id).thenReturn(userId)

    useCase = new ReviewAllCommitsUseCase(eventBus, commitInfoDao, repositoriesCache, toReviewBranchCommitsFilter, reviewedCommitsCache)

    // given
    when(repositoriesCache.getBranchCommits(RepoName, BranchName)).thenReturn(List(commitCache))
    when(toReviewBranchCommitsFilter.filterCommitsToReview(List(commitCache), user, RepoName)).thenReturn(List(commit.sha))
    when(commitInfoDao.findByShaList(RepoName, List(commit.sha))).thenReturn(List(PartialCommitInfo(commit)))
  }

  it should "generate all commits reviewed event" in {
    // when
    useCase.execute(RepoName, BranchName, user)

    // then
    verify(eventBus).publish(AllCommitsReviewedEvent(RepoName, BranchName, user.id))
  }

  it should "mark all commits as reviewed if commits were found" in {
    // when
    useCase.execute(RepoName, BranchName, user)

    // then
    val expectedCommitReviewed = ReviewedCommit(commit.sha, user.id, RepoName, clock.nowUtc)
    verify(reviewedCommitsCache).markCommitAsReviewed(expectedCommitReviewed)
  }

}
