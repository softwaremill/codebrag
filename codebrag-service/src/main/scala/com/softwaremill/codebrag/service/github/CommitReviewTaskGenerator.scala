package com.softwaremill.codebrag.service.github

import com.softwaremill.codebrag.dao.{CommitReviewTaskDAO, UserDAO}
import com.softwaremill.codebrag.domain.CommitInfo

class CommitReviewTaskGenerator(userDao: UserDAO, commitToReviewDao: CommitReviewTaskDAO) {

  def createReviewTasksFor(commit: CommitInfo) {
    commit.createReviewTasksFor(repositoryUsers()).foreach(reviewTask => commitToReviewDao.save(reviewTask))
  }

  def repositoryUsers() = {
    userDao.findAll()
  }

}