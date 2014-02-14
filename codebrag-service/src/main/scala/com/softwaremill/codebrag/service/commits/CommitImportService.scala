package com.softwaremill.codebrag.service.commits

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.{UpdatedCommit, CommitsUpdatedEvent}
import com.softwaremill.codebrag.common.{Clock, EventBus}
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.repository.config.RepoData

class CommitImportService(commitsLoader: CommitsLoader, commitInfoDao: CommitInfoDAO, eventBus: EventBus)(implicit clock: Clock) extends Logging {

  def importRepoCommits(repoConfig: RepoData) {
    logger.debug("Start loading commits")
    val commitsLoaded = commitsLoader.loadNewCommits(repoConfig)
    logger.debug(s"Commits loaded: ${commitsLoaded.size}")
    commitsLoaded.foreach(commitInfoDao.storeCommit)
    if (!commitsLoaded.isEmpty) {
      val isFirstImport = !commitInfoDao.hasCommits
      eventBus.publish(CommitsUpdatedEvent(isFirstImport, commitsLoaded.map(commit =>
        UpdatedCommit(commit.id, commit.authorName, commit.authorEmail, commit.commitDate))))
    }
    logger.debug("Commits stored. Loading finished.")
  }

}