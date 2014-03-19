package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.domain.{ReviewedCommit, CommitReviewTask}
import com.softwaremill.codebrag.common.{EventBus, Clock}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.service.commits.branches.ReviewedCommitsCache
import com.softwaremill.codebrag.domain.reactions.CommitReviewedEvent

/**
 * Handles user activity when user wants to mark given commit as reviewed
 */
class CommitReviewActivity(
  commitDao: CommitInfoDAO,
  reviewedCommitsCache: ReviewedCommitsCache,
  eventBus: EventBus) (implicit clock: Clock) extends Logging {

  def markAsReviewed(task: CommitReviewTask) {
    commitDao.findByCommitId(task.commitId).foreach { commit =>
      val reviewedCommit = ReviewedCommit(commit.sha, task.userId, clock.nowUtc)
      reviewedCommitsCache.markCommitAsReviewed(reviewedCommit)
      eventBus.publish(CommitReviewedEvent(commit, task.userId))
    }

  }

}
