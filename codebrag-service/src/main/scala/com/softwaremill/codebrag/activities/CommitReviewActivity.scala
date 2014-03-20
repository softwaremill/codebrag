package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.domain.ReviewedCommit
import com.softwaremill.codebrag.common.{EventBus, Clock}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.service.commits.branches.UserReviewedCommitsCache
import com.softwaremill.codebrag.domain.reactions.CommitReviewedEvent
import org.bson.types.ObjectId

/**
 * Handles user activity when user wants to mark given commit as reviewed
 */
class CommitReviewActivity(
  commitDao: CommitInfoDAO,
  reviewedCommitsCache: UserReviewedCommitsCache,
  eventBus: EventBus) (implicit clock: Clock) extends Logging {

  def markAsReviewed(commitId: ObjectId, userId: ObjectId) {
    commitDao.findByCommitId(commitId).foreach { commit =>
      val reviewedCommit = ReviewedCommit(commit.sha, userId, clock.nowUtc)
      reviewedCommitsCache.markCommitAsReviewed(reviewedCommit)
      eventBus.publish(CommitReviewedEvent(commit, userId))
    }

  }

}
