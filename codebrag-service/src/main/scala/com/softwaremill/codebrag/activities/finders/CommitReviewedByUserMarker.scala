package com.softwaremill.codebrag.activities.finders

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.finders.views.CommitView
import com.softwaremill.codebrag.service.commits.branches.UserReviewedCommitsCache
import com.softwaremill.codebrag.domain.{CommitAuthorClassification, ReviewedCommit}
import com.softwaremill.codebrag.dao.user.UserDAO

trait CommitReviewedByUserMarker {

  def reviewedCommitsCache: UserReviewedCommitsCache
  def userDao: UserDAO

  def markAsReviewed(commitsViews: List[CommitView], userId: ObjectId) = {
    val alreadyReviewed = commitsReviewedByUser(userId)
    commitsViews.map(markIfReviewed(userId, _, alreadyReviewed))
  }

  def markAsReviewed(commitView: CommitView, userId: ObjectId) = {
    val alreadyReviewed = commitsReviewedByUser(userId)
    markIfReviewed(userId, commitView, alreadyReviewed)
  }

  private def markIfReviewed(userId: ObjectId, commitView: CommitView, alreadyReviewed: Set[ReviewedCommit]) = {
    val userIsAuthor = userDao.findById(userId) match {
      case Some(user) => CommitAuthorClassification.commitAuthoredByUser(commitView, user)
      case None => true  // should not happen, but if yes mark user as author
    }
    val toReview = if(userIsAuthor) {
      false
    } else {
      alreadyReviewed.find(_.sha == commitView.sha).isEmpty
    }
    commitView.copy(pendingReview = toReview)
  }

  private def commitsReviewedByUser(userId: ObjectId) = reviewedCommitsCache.getUserEntry(userId).commits

}
