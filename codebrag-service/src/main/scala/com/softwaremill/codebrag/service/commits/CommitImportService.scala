package com.softwaremill.codebrag.service.commits

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.{UpdatedCommit, CommitsUpdatedEvent}
import com.softwaremill.codebrag.common.{Clock, EventBus}
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.repository.config.RepoData

class CommitImportService(commitsLoader: CommitsLoader, commitInfoDao: CommitInfoDAO, eventBus: EventBus)(implicit clock: Clock) extends Logging {

  def importRepoCommits(repoData: RepoData) {
    logger.debug("Start loading commits")
    val loadCommitsResult = commitsLoader.loadNewCommits(repoData)
    logger.debug(s"Commits loaded: ${loadCommitsResult.commits.size}")
    loadCommitsResult.commits.foreach(commitInfoDao.storeCommit)
    if (!loadCommitsResult.commits.isEmpty) {
      val isFirstImport = !commitInfoDao.hasCommits
      eventBus.publish(CommitsUpdatedEvent(isFirstImport, loadCommitsResult.commits.map(UpdatedCommit(_))))
    }
    logger.debug("Commits stored. Loading finished.")
  }

}