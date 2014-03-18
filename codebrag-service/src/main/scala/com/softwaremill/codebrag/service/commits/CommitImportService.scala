package com.softwaremill.codebrag.service.commits

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.RepositoryStatus
import com.softwaremill.codebrag.common.{Clock, EventBus}
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO
import com.softwaremill.codebrag.service.commits.branches.RepositoryCache
import com.softwaremill.codebrag.repository.Repository

class CommitImportService(repoStatusDao: RepositoryStatusDAO, eventBus: EventBus, cache: RepositoryCache)(implicit clock: Clock) extends Logging {

  def importRepoCommits(repository: Repository) {
    try {
      repository.pullChanges()
      val loaded = repository.loadCommitsSince(repoStatusDao.loadBranchesState)
      cache.addCommits(loaded)
      updateRepoReadyStatus(repository.repoName)
    } catch {
      case e: Exception => {
        logger.error("Cannot import repository commits", e)
        updateRepoNotReadyStatus(repository.repoName, e.getMessage)
      }
    }
  }

  private def updateRepoNotReadyStatus(repoName: String, errorMsg: String) {
    logger.debug(s"Saving repository-not-ready status data to DB with message: $errorMsg")
    val repoNotReadyStatus = RepositoryStatus.notReady(repoName, Some(errorMsg))
    repoStatusDao.updateRepoStatus(repoNotReadyStatus)
  }

  private def updateRepoReadyStatus(repoName: String) {
    logger.debug(s"Saving repository-ready status data to DB")
    val repoReadyStatus = RepositoryStatus.ready(repoName)
    repoStatusDao.updateRepoStatus(repoReadyStatus)
  }

}