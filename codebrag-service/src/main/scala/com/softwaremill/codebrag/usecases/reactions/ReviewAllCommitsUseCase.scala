package com.softwaremill.codebrag.usecases.reactions

import com.softwaremill.codebrag.cache.{RepositoriesCache, UserReviewedCommitsCache}
import com.softwaremill.codebrag.common.{EventBus, Clock}
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.domain.{ReviewedCommit, User}
import com.softwaremill.codebrag.domain.reactions.AllCommitsReviewedEvent
import com.softwaremill.codebrag.finders.commits.toreview.ToReviewBranchCommitsFilter
import com.typesafe.scalalogging.slf4j.Logging

/**
 * Handles user activity when user wants to mark all commits as reviewed
 */
class ReviewAllCommitsUseCase(
  eventBus: EventBus,
  commitInfoDAO: CommitInfoDAO,
  repoCache: RepositoriesCache,
  toReviewCommitsFilter: ToReviewBranchCommitsFilter,
  reviewedCommitsCache: UserReviewedCommitsCache
)
  (implicit clock: Clock) extends Logging {

  type ReviewAllCommitsResult = Either[String, Unit]

  def execute(repoName: String, branchName: String, user: User): ReviewAllCommitsResult = {

    val allBranchCommits = repoCache.getBranchCommits(repoName, branchName)
    val toReviewBranchCommits = toReviewCommitsFilter.filterCommitsToReview(allBranchCommits, user, repoName)

    commitInfoDAO.findByShaList(repoName, toReviewBranchCommits).foreach { commit =>
      val reviewedCommit = ReviewedCommit(commit.sha, user.id, repoName, clock.nowUtc)
      reviewedCommitsCache.markCommitAsReviewed(reviewedCommit)
    }

    Right {
      eventBus.publish(AllCommitsReviewedEvent(repoName, branchName, user.id))
    }
  }

}
