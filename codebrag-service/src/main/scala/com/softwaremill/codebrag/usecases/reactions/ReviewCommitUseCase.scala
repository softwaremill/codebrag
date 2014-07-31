package com.softwaremill.codebrag.usecases.reactions

import com.softwaremill.codebrag.domain.{CommitInfo, ReviewedCommit}
import com.softwaremill.codebrag.common.{EventBus, Clock}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.domain.reactions.CommitReviewedEvent
import org.bson.types.ObjectId
import com.softwaremill.codebrag.cache.UserReviewedCommitsCache
import com.softwaremill.codebrag.licence.LicenceService

/**
 * Handles user activity when user wants to mark given commit as reviewed
 */
class ReviewCommitUseCase(
  commitDao: CommitInfoDAO,
  reviewedCommitsCache: UserReviewedCommitsCache,
  eventBus: EventBus, licenceService: LicenceService) (implicit clock: Clock) extends Logging {

  type ReviewCommitResult = Either[String, Unit]

  def execute(repoName: String, sha: String, userId: ObjectId): ReviewCommitResult = {
    ifCanExecute(repoName, sha, userId) {
      commitDao.findBySha(repoName, sha) match {
        case Some(commit) => Right(review(userId, commit))
        case None => Left("Cannot find commit to review")
      }
    }
  }

  private def review(userId: ObjectId, commit: CommitInfo) = {
    val reviewedCommit = ReviewedCommit(commit.sha, userId, commit.repoName, clock.nowUtc)
    reviewedCommitsCache.markCommitAsReviewed(reviewedCommit)
    eventBus.publish(CommitReviewedEvent(commit, userId))
  }

  protected def ifCanExecute(repoName: String, sha: String, userId: ObjectId)(block: => ReviewCommitResult): ReviewCommitResult = {
    licenceService.interruptIfLicenceExpired()
    block
  }

}
