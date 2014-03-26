package com.softwaremill.codebrag.activities.finders

import com.softwaremill.codebrag.dao.finders.views.{CommitView, CommitReviewerView}
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.cache.UserReviewedCommitsCache

trait ReviewersDataAppender {

  def reviewedCommitsCache: UserReviewedCommitsCache
  def userDao: UserDAO

  def addReviewersData(view: CommitView, sha: String) = {
    val reviewers = reviewedCommitsCache.usersWhoReviewed(sha).flatMap(userDao.findById).map(CommitReviewerView.apply)
    view.copy(reviewers = Some(reviewers))
  }

}
