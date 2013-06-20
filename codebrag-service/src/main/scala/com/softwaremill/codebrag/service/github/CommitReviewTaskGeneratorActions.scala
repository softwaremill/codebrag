package com.softwaremill.codebrag.service.github

import com.softwaremill.codebrag.domain._
import com.softwaremill.codebrag.dao.{CommitInfoDAO, CommitReviewTaskDAO, UserDAO}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.CommitsUpdatedEvent
import com.softwaremill.codebrag.domain.CommitReviewTask
import com.softwaremill.codebrag.domain.UpdatedCommit
import com.softwaremill.codebrag.domain.CommitUpdatedEvent._
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.domain.CommitAuthorClassification._

trait CommitReviewTaskGeneratorActions extends Logging {

  val userDao: UserDAO
  val commitToReviewDao: CommitReviewTaskDAO
  val commitInfoDao: CommitInfoDAO

  def handleNewUserRegistered(event: NewUserRegistered) {
    val commitsToReview = commitInfoDao.findLast(CommitReviewTaskGeneratorActions.LastCommitsToReviewCount)
    val tasks = commitsToReview.filterNot(commitAuthoredByUser(_, event)).map(commit => {CommitReviewTask(commit.id, event.id)})
    logger.debug(s"Generating ${tasks.length} tasks for newly registered user: $event")
    tasks.foreach(commitToReviewDao.save(_))
  }

  def handleCommitsUpdated(event: CommitsUpdatedEvent) {
    val commitsToGenerateTasks = if (event.firstTime)
      event.newCommits.take(CommitReviewTaskGeneratorActions.LastCommitsToReviewCount)
    else event.newCommits
    commitsToGenerateTasks.foreach(createAndStoreReviewTasksFor(_))
  }

  private def createAndStoreReviewTasksFor(commit: UpdatedCommit) {
    val repoUsers = repositoryUsers()
    val tasks = createReviewTasksFor(commit, repoUsers)
    tasks.foreach(commitToReviewDao.save(_))
  }

  // TODO: return only repository users instead of all users as soon as permissions model is implemented
  private def repositoryUsers() = {
    userDao.findAll()
  }

  private def createReviewTasksFor(commit: UpdatedCommit, users: List[User]) = {
    users.filterNot(commitAuthoredByUser(commit, _)).map(user => CommitReviewTask(commit.id, user.id))
  }

}

object CommitReviewTaskGeneratorActions {

  val LastCommitsToReviewCount = 10
}