package com.softwaremill.codebrag.service.github

import com.softwaremill.codebrag.dao.{CommitReviewTaskDAO, UserDAO}
import com.softwaremill.codebrag.domain.{User, UpdatedCommit, CommitReviewTask, CommitsUpdatedEvent}
import akka.actor.Actor

class CommitReviewTaskGenerator(userDao: UserDAO, commitToReviewDao: CommitReviewTaskDAO) extends Actor {

  val MaxCommitsForFirstImport = 20

  def receive = {
    case (CommitsUpdatedEvent(firstTime, newCommits)) => {
      val commitsToGenerateTasks = if (firstTime) newCommits.take(MaxCommitsForFirstImport) else newCommits
      commitsToGenerateTasks.foreach(createAndStoreReviewTasksFor(_))
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