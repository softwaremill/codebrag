package com.softwaremill.codebrag.service.commits

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.{RepositoryStatus, LightweightCommitInfo, NewCommitsLoadedEvent}
import com.softwaremill.codebrag.common.{Clock, EventBus}
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.repository.config.RepoData
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO

class CommitImportService(commitsLoader: CommitsLoader, commitInfoDao: CommitInfoDAO, repoStatusDao: RepositoryStatusDAO, eventBus: EventBus)(implicit clock: Clock) extends Logging {

  def importRepoCommits(repoData: RepoData) {
    try {
      doImport(repoData)
    } catch {
      case e: Exception => {
        logger.error("Cannot import repository commits", e)
        updateRepoNotReadyStatus(repoData.repoName, e.getMessage)
      }
    }
  }

  private def doImport(repoData: RepoData) {
    logger.debug("Start loading commits")
    val loadCommitsResult = commitsLoader.loadNewCommits(repoData)
    logger.debug(s"Commits loaded: ${loadCommitsResult.commits.size}")
    loadCommitsResult.commits.foreach(commitInfoDao.storeCommit)
    val isFirstImport = !commitInfoDao.hasCommits   // TODO: don't like this hacky flag, would like to refactor it some day
    eventBus.publish(NewCommitsLoadedEvent(isFirstImport, loadCommitsResult.repoName, loadCommitsResult.currentRepoHeadSHA, loadCommitsResult.commits.map(LightweightCommitInfo(_))))
    logger.debug("Commits stored. Loading finished.")
  }

  private def updateRepoNotReadyStatus(repoName: String, errorMsg: String) {
    logger.debug(s"Saving repository-not-ready status data to DB with message: ${errorMsg}")
    val repoNotReadyStatus = RepositoryStatus.notReady(repoName, Some(errorMsg))
    repoStatusDao.updateRepoStatus(repoNotReadyStatus)
  }

}