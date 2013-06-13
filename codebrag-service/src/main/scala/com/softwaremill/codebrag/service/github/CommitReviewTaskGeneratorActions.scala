package com.softwaremill.codebrag.service.github

import com.softwaremill.codebrag.dao.events.NewUserRegistered
import org.joda.time.Interval
import com.softwaremill.codebrag.domain.{User, UpdatedCommit, CommitReviewTask}
import com.softwaremill.codebrag.dao.{CommitInfoDAO, CommitReviewTaskDAO, UserDAO}
import pl.softwaremill.common.util.time.Clock
import com.typesafe.scalalogging.slf4j.Logging

trait CommitReviewTaskGeneratorActions extends Logging {

  val userDao: UserDAO
  val commitToReviewDao: CommitReviewTaskDAO
  val commitInfoDao: CommitInfoDAO
  val clock: Clock

  def handleNewUserRegistered(event: NewUserRegistered) {
    val now = clock.currentDateTime()
    val lastWeekInterval = new Interval(now.minusDays(7), now)
    val commitsToReview = commitInfoDao.findForTimeRange(lastWeekInterval)
    val tasks = commitsToReview.filterNot(_.authorName == event.fullName).map(commit => {CommitReviewTask(commit.id, event.id)})
    logger.debug(s"Generating ${tasks.length} tasks for newly registered user: $event")
    tasks.foreach(commitToReviewDao.save(_))
  }

  protected def createAndStoreReviewTasksFor(commit: UpdatedCommit) {
    val repoUsers = repositoryUsers()
    val tasks = createReviewTasksFor(commit, repoUsers)
    tasks.foreach(commitToReviewDao.save(_))
  }

  // TODO: return only repository users instead of all users as soon as permissions model is implemented
  private def repositoryUsers() = {
    userDao.findAll()
  }

  protected def createReviewTasksFor(commit: UpdatedCommit, users: List[User]) = {
    users.filterNot(_.name == commit.authorName).map(user => CommitReviewTask(commit.id, user.id))
  }

}
