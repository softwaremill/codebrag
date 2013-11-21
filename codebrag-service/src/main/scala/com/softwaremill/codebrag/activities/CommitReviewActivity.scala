package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.domain.CommitReviewTask
import com.softwaremill.codebrag.dao.{CommitInfoDAO, CommitReviewTaskDAO}
import com.softwaremill.codebrag.common.{EventBus, Clock}
import com.softwaremill.codebrag.domain.reactions.CommitReviewedEvent
import com.typesafe.scalalogging.slf4j.Logging

/**
 * Handles user activity when user wants to mark given commit as reviewed
 */
class CommitReviewActivity(commitReviewTaskDao: CommitReviewTaskDAO,
                           commitDao: CommitInfoDAO,
                           eventBus: EventBus)
                          (implicit clock: Clock)
  extends Logging {

  def markAsReviewed(task: CommitReviewTask) {
    commitReviewTaskDao.delete(task)
    commitDao.findByCommitId(task.commitId) match {
      case Some(commit) => eventBus.publish(CommitReviewedEvent(commit, task.userId))
      case None => logger.warn(s"Commit with ${task.commitId} doesn't exist!")
    }
  }

}
