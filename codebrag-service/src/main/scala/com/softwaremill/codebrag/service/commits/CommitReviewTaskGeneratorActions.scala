package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.domain._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.NewCommitsLoadedEvent
import com.softwaremill.codebrag.domain.CommitReviewTask
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.domain.CommitAuthorClassification._
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.reviewtask.CommitReviewTaskDAO
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO

trait CommitReviewTaskGeneratorActions extends Logging {

  val userDao: UserDAO
  val commitToReviewDao: CommitReviewTaskDAO
  val commitInfoDao: CommitInfoDAO
  val repoStatusDao: RepositoryStatusDAO

  def handleNewUserRegistered(event: NewUserRegistered) {
    val commitsToReview = commitInfoDao.findLastCommitsNotAuthoredByUser(event, CommitReviewTaskGeneratorActions.LastCommitsToReviewCount)
    val tasks = commitsToReview.map(commit => {CommitReviewTask(commit.id, event.id)})
    logger.debug(s"Generating ${tasks.length} tasks for newly registered user: $event")
    tasks.foreach(commitToReviewDao.save(_))
  }

  def handleCommitsUpdated(event: NewCommitsLoadedEvent) {
    val commits = chooseCommitsToGenerateTasksFor(event)
    userDao.findAll().foreach(createAndStoreReviewTasksFor(commits, _))
    updateRepoReadyStatus(event.repoName, event.currentSHA)
  }

  private def chooseCommitsToGenerateTasksFor(event: NewCommitsLoadedEvent): List[LightweightCommitInfo] = {
    if (event.firstTime) {
      event.newCommits.take(CommitReviewTaskGeneratorActions.LastCommitsToReviewCount)
    } else {
      event.newCommits
    }
  }

  private def createAndStoreReviewTasksFor(commits: List[LightweightCommitInfo], user: User) {
    constructReviewTasksFor(commits, user).foreach(commitToReviewDao.save(_))
  }

  private def constructReviewTasksFor(commits: List[LightweightCommitInfo], user: User) = {
    commits.filterNot(commitAuthoredByUser(_, user)).map(commit => CommitReviewTask(commit.id, user.id))
  }

  private def updateRepoReadyStatus(repoName: String, currentHEAD: String) {
    logger.debug(s"Saving repository-ready status data to DB with HEAD: ${repoName}")
    val repoReadyStatus = RepositoryStatus.ready(repoName).withHeadId(currentHEAD)
    repoStatusDao.updateRepoStatus(repoReadyStatus)
  }

}

object CommitReviewTaskGeneratorActions {
  val LastCommitsToReviewCount = 10
}