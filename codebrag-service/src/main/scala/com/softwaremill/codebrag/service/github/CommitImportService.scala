package com.softwaremill.codebrag.service.github

import com.softwaremill.codebrag.dao.CommitInfoDAO
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.{UpdatedCommit, CommitsUpdatedEvent}
import com.softwaremill.codebrag.common.EventBus

class CommitImportService(commitsLoader: CommitsLoader, commitInfoDao: CommitInfoDAO, eventBus: EventBus) extends Logging {

  def importRepoCommits(repoData: RepoData) {
    logger.debug("Start loading commits")
    val commitsLoaded = commitsLoader.loadMissingCommits(repoData)
    logger.debug(s"Commits loaded: ${commitsLoaded.size}")
    val isFirstImport = !commitInfoDao.hasCommits
    commitsLoaded.foreach(commitInfoDao.storeCommit(_))

    if (!commitsLoaded.isEmpty) {
      eventBus.publish(CommitsUpdatedEvent(isFirstImport, commitsLoaded.map(commit =>
        UpdatedCommit(commit.id, commit.authorName, commit.commitDate))))
    }
    logger.debug("Commits stored. Loading finished.")
  }
}