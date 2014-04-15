package com.softwaremill.codebrag.activities.finders

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.finders.views.{CommitState, CommitView}
import com.softwaremill.codebrag.domain.{CommitAuthorClassification, ReviewedCommit}
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.cache.{BranchCommitCacheEntry, BranchCommitsCache, UserReviewedCommitsCache}
import org.joda.time.DateTime
import com.softwaremill.codebrag.service.config.ReviewProcessConfig

trait CommitReviewStateAppender {

  def reviewedCommitsCache: UserReviewedCommitsCache
  def userDao: UserDAO
  def config: ReviewProcessConfig

  def setCommitsReviewStates(commitsViews: List[CommitView], userId: ObjectId) = {
    commitsViews.map(setCommitReviewState(_, userId))
  }

  def setCommitReviewState(commitView: CommitView, userId: ObjectId) = {
    commitView.copy(state = calculateState(commitView, userId))
  }


  def calculateState(commitView: CommitView, userId: ObjectId): CommitState.Value = {
    if(commitTooOldForUser(commitView, userId)) {
      return CommitState.NotApplicable
    }
    if (reviewedByUser(commitView, userId)) {
      return CommitState.ReviewedByUser
    }
    if(fullyReviewed(commitView)) {
      return CommitState.ReviewedByOthers
    }
    if(isUserAnAuthor(commitView, userId)) {
      return CommitState.AwaitingOthersReview
    }
    CommitState.AwaitingUserReview
  }

  def reviewedByUser(commitView: CommitView, userId: ObjectId): Boolean = {
    reviewedCommitsCache.reviewedByUser(commitView.sha, userId)
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

  private def commitTooOldForUser(commit: CommitView, userId: ObjectId) = {
    val userDate = reviewedCommitsCache.getUserEntry(userId).toReviewStartDate
    val commitDate = new DateTime(commit.date)
    commitDate.isBefore(userDate)
  }

}
