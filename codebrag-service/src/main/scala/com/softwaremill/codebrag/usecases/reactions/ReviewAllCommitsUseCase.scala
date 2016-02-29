package com.softwaremill.codebrag.usecases.reactions

import com.softwaremill.codebrag.cache.UserReviewedCommitsCache
import com.softwaremill.codebrag.common.{EventBus, Clock}
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.ReviewedCommit
import com.softwaremill.codebrag.domain.reactions.AllCommitsReviewedEvent
import com.softwaremill.codebrag.finders.browsingcontext.UserBrowsingContext
import com.typesafe.scalalogging.slf4j.Logging

/**
 * Handles user activity when user wants to mark all commits as reviewed
 */
class ReviewAllCommitsUseCase(
  userDao: UserDAO,
  eventBus: EventBus,
  commitInfoDAO: CommitInfoDAO,
  reviewedCommitsCache: UserReviewedCommitsCache
)
  (implicit clock: Clock) extends Logging {

  type ReviewAllCommitsResult = Either[String, Unit]

  def execute(context: UserBrowsingContext, toReviewBranchCommits: List[String]): ReviewAllCommitsResult = {

    val result = for {
      user <- userDao.findById(context.userId)
    } yield {
      logger.debug(s"Using $user to find all commits to review")


      logger.debug(s"Found ${toReviewBranchCommits.size} commits to mark as reviewed")

      commitInfoDAO.findByShaList(context.repoName, toReviewBranchCommits).foreach { commit =>
        val reviewedCommit = ReviewedCommit(commit.sha, user.id, context.repoName, clock.nowUtc)

        logger.info(s"Marking commit ${reviewedCommit.sha} as reviewed by user $user")
        reviewedCommitsCache.markCommitAsReviewed(reviewedCommit)
      }

      eventBus.publish(AllCommitsReviewedEvent(context.repoName, context.branchName, context.userId))
    }

    result.toRight {
      s"Cannot mark all commits in branch ${context.branchName} as reviewed by user ${context.userId}"
    }
  }

}
