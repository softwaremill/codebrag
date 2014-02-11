package com.softwaremill.codebrag.dao.finders.commit

import com.softwaremill.codebrag.dao.reporting.views.{CommitListView, CommitView}
import org.bson.types.ObjectId
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.dao.reviewtask.CommitReviewTaskRecord


trait CommitReviewedByUserMarker {

  def markAsReviewed(commitsViews: List[CommitView], userId: ObjectId) = {
    val remainingToReview = commitsPendingReviewFor(userId)
    CommitListView(commitsViews.map(markIfReviewed(_, remainingToReview)), 0, 0)
  }

  def markAsReviewed(commitView: CommitView, userId: ObjectId) = {
    val remainingToReview = commitsPendingReviewFor(userId)
    markIfReviewed(commitView, remainingToReview)
  }

  private def markIfReviewed(commitView: CommitView, remainingToReview: Set[ObjectId]) = {
    if (remainingToReview.contains(new ObjectId(commitView.id)))
      commitView
    else
      commitView.copy(pendingReview = false)
  }

  private def commitsPendingReviewFor(userId: ObjectId) = {
    val userReviewTasks = CommitReviewTaskRecord.where(_.userId eqs userId).fetch()
    userReviewTasks.map(_.commitId.get).toSet
  }

}
