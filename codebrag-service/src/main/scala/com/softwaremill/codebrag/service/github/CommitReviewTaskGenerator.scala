package com.softwaremill.codebrag.service.github

import com.softwaremill.codebrag.dao.{CommitReviewTaskDAO, UserDAO}
import com.softwaremill.codebrag.domain.CommitInfo

class CommitReviewTaskGenerator(userDao: UserDAO, commitToReviewDao: CommitReviewTaskDAO) {

  def createReviewTasksFor(commit: CommitInfo) {
    commit.createReviewTasksFor(repositoryUsers()).foreach(reviewTask => commitToReviewDao.save(reviewTask))
  }

  // TODO: return only repository users instead of all users as soon as permissions model is implemented
  def repositoryUsers() = {
    userDao.findAll()
  }

}