package com.softwaremill.codebrag.service.github

import com.softwaremill.codebrag.dao.{CommitInfoDAO, CommitReviewTaskDAO, UserDAO}
import com.softwaremill.codebrag.domain.{User, UpdatedCommit, CommitReviewTask, CommitsUpdatedEvent}
import akka.actor.Actor
import pl.softwaremill.common.util.time.Clock
import org.joda.time.{Interval, DateTime}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.events.NewUserRegistered

class CommitReviewTaskGenerator(userDao: UserDAO, commitToReviewDao: CommitReviewTaskDAO, commitInfoDao: CommitInfoDAO, clock: Clock) extends Actor with Logging {

  val MaxCommitsForFirstImport = 20

  def receive = {
    case (CommitsUpdatedEvent(firstTime, newCommits)) => {
      val commitsToGenerateTasks = if (firstTime) newCommits.take(MaxCommitsForFirstImport) else newCommits
      commitsToGenerateTasks.foreach(createAndStoreReviewTasksFor(_))
    }

    case (event@NewUserRegistered(userId, login, fullName, email)) => {
      val now = clock.currentDateTime()
      val lastWeekInterval = new Interval(now.minusDays(7), now)
      val commitsToReview = commitInfoDao.findForTimeRange(lastWeekInterval)
      val tasks = commitsToReview.filterNot(_.authorName == fullName).map(commit => {CommitReviewTask(commit.id, userId)})
      logger.debug(s"Generating ${tasks.length} tasks for newly registered user: $event")
      tasks.foreach(commitToReviewDao.save(_))
    }
  }

  private def createAndStoreReviewTasksFor(commit: UpdatedCommit) {
    val repoUsers = repositoryUsers()
    val tasks = CommitReviewTaskGenerator.createReviewTasksFor(commit, repoUsers)
    tasks.foreach(commitToReviewDao.save(_))
  }

  // TODO: return only repository users instead of all users as soon as permissions model is implemented
  private def repositoryUsers() = {
    userDao.findAll()
  }

}

object CommitReviewTaskGenerator {
  def createReviewTasksFor(commit: UpdatedCommit, users: List[User]) = {
    users.filterNot(_.name == commit.authorName).map(user => CommitReviewTask(commit.id, user.id))
  }
}