package com.softwaremill.codebrag.usecases.reactions

import com.softwaremill.codebrag.cache.{RepositoriesCache, UserReviewedCommitsCache}
import com.softwaremill.codebrag.common.{EventBus, Clock}
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.ReviewedCommit
import com.softwaremill.codebrag.domain.reactions.AllCommitsReviewedEvent
import com.softwaremill.codebrag.finders.commits.toreview.ToReviewBranchCommitsFilter
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId

/**
 * Handles user activity when user wants to mark all commits as reviewed
 */
class ReviewAllCommitsUseCase(
  userDao: UserDAO,
  eventBus: EventBus,
  commitInfoDAO: CommitInfoDAO,
  repoCache: RepositoriesCache,
  toReviewCommitsFilter: ToReviewBranchCommitsFilter,
  reviewedCommitsCache: UserReviewedCommitsCache
)
  (implicit clock: Clock) extends Logging {

  type ReviewAllCommitsResult = Either[String, Unit]

  def execute(repoName: String, branchName: String, userId: ObjectId): ReviewAllCommitsResult = {

    val result = for {
      user <- userDao.findById(userId)
    } yield {
      logger.debug(s"Using $user to find all commits to review")

      val allBranchCommits = repoCache.getBranchCommits(repoName, branchName)
      val toReviewBranchCommits = toReviewCommitsFilter.filterCommitsToReview(allBranchCommits, user, repoName)

      logger.debug(s"Found ${toReviewBranchCommits.size} commits to mark as reviewed")

      commitInfoDAO.findByShaList(repoName, toReviewBranchCommits).foreach { commit =>
        val reviewedCommit = ReviewedCommit(commit.sha, user.id, repoName, clock.nowUtc)

        logger.info(s"Marking commit ${reviewedCommit.sha} as reviewed by user $user")
        reviewedCommitsCache.markCommitAsReviewed(reviewedCommit)
      }

      eventBus.publish(AllCommitsReviewedEvent(repoName, branchName, userId))
    }

    result.toRight {
      s"Cannot mark all commits in branch $branchName as reviewed by user $userId"
    }
  }

}
