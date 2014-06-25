package com.softwaremill.codebrag.finders.commits

import com.softwaremill.codebrag.dao.finders.views.{CommitView, CommitReviewerView}
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.cache.UserReviewedCommitsCache

trait ReviewersDataAppender {

  def reviewedCommitsCache: UserReviewedCommitsCache
  def userDao: UserDAO

  def addReviewersData(view: CommitView) = {
    val reviewers = reviewedCommitsCache.usersWhoReviewed(view.repoName, view.sha).flatMap(userDao.findById).map(CommitReviewerView.apply)
    view.copy(reviewers = reviewers)
  }

}
