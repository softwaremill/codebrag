package com.softwaremill.codebrag.activities.finders

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.finders.views.{CommitReviewState, CommitView}
import com.softwaremill.codebrag.domain.{CommitAuthorClassification, ReviewedCommit}
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.cache.{BranchCommitCacheEntry, BranchCommitsCache, UserReviewedCommitsCache}
import org.joda.time.DateTime
import com.softwaremill.codebrag.service.config.ReviewProcessConfig

trait CommitReviewedByUserMarker {

  def reviewedCommitsCache: UserReviewedCommitsCache
  def userDao: UserDAO
  def config: ReviewProcessConfig

  def markAsReviewed(commitsViews: List[CommitView], userId: ObjectId) = {
    commitsViews.map(markIfReviewed(userId, _))
  }

  def addReviewedFlag(commitView: CommitView, userId: ObjectId) = {
    markIfReviewed(userId, commitView)
  }

  private def markIfReviewed(userId: ObjectId, commitView: CommitView) = {
    val commitState = if(commitAlreadyReviewedByUser(commitView, userId)) {
      CommitReviewState.Reviewed
    } else if(isUserAnAuthor(commitView, userId) || reviewIsNotRequired(commitView, userId) || fullyReviewed(commitView)) {
      CommitReviewState.ReviewNotRequired
    } else {
      CommitReviewState.AwaitingReview
    }
    commitView.copy(state = commitState)
  }

  private def commitAlreadyReviewedByUser(commit: CommitView, userId: ObjectId) = {
    reviewedCommitsCache.getUserEntry(userId).commits.find(_.sha == commit.sha).nonEmpty
  }

  def fullyReviewed(commit: CommitView): Boolean = {
    reviewedCommitsCache.usersWhoReviewed(commit.sha).size >= config.requiredReviewersCount
  }

  private def isUserAnAuthor(commit: CommitView, userId: ObjectId) = {
    userDao.findById(userId) match {
      case Some(user) => CommitAuthorClassification.commitAuthoredByUser(commit, user)
      case None => true  // should not happen, but if yes mark user as author
    }
  }

  private def reviewIsNotRequired(commit: CommitView, userId: ObjectId) = {
    val userDate = reviewedCommitsCache.getUserEntry(userId).toReviewStartDate
    val commitDate = new DateTime(commit.date)
    commitDate.isBefore(userDate)
  }

}
