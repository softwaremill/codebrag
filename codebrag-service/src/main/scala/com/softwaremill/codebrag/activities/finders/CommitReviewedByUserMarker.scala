package com.softwaremill.codebrag.activities.finders

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.finders.views.CommitView
import com.softwaremill.codebrag.service.commits.branches.ReviewedCommitsCache
import com.softwaremill.codebrag.domain.ReviewedCommit

trait CommitReviewedByUserMarker {

  def reviewedCommitsCache: ReviewedCommitsCache

  def markAsReviewed(commitsViews: List[CommitView], userId: ObjectId) = {
    val remainingToReview = commitsReviewedByUser(userId)
    commitsViews.map(markIfReviewed(_, remainingToReview))
  }

  def markAsReviewed(commitView: CommitView, userId: ObjectId) = {
    val remainingToReview = commitsReviewedByUser(userId)
    markIfReviewed(commitView, remainingToReview)
  }

  private def markIfReviewed(commitView: CommitView, remainingToReview: Set[ReviewedCommit]) = {
    if (remainingToReview.find(_.sha == commitView.sha).isEmpty)
      commitView
    else
      commitView.copy(pendingReview = false)
  }

  private def commitsReviewedByUser(userId: ObjectId) = reviewedCommitsCache.reviewedByUser(userId)

}
