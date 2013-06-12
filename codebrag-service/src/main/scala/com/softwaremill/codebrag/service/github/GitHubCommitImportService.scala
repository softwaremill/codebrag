package com.softwaremill.codebrag.service.github

import com.softwaremill.codebrag.dao.CommitInfoDAO
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.events.EventBus
import com.softwaremill.codebrag.domain.{UpdatedCommit, CommitsUpdatedEvent}

class GitHubCommitImportService(commitsLoader: GitHubCommitsLoader, commitInfoDao: CommitInfoDAO, eventBus: EventBus) extends Logging {

  def importRepoCommits(owner: String, repo: String) {
    logger.debug("Start loading commits")
    val commitsLoaded = commitsLoader.loadMissingCommits(owner, repo)
    logger.debug(s"Commits loaded: ${commitsLoaded.size}")
    val isFirstImport = !commitInfoDao.hasCommits
    commitsLoaded.foreach(commitInfoDao.storeCommit(_))

    if (!commitsLoaded.isEmpty) {
      eventBus.publish(CommitsUpdatedEvent(isFirstImport, commitsLoaded.map(commit => UpdatedCommit(commit.id, commit.authorName))))
    }
    logger.debug("Commits stored. Loading finished.")
  }
}