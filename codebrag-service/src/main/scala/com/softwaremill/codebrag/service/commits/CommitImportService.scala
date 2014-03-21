package com.softwaremill.codebrag.service.commits

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.RepositoryStatus
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO
import com.softwaremill.codebrag.service.commits.branches.BranchCommitsCache
import com.softwaremill.codebrag.repository.Repository
import com.softwaremill.codebrag.dao.branchsnapshot.BranchStateDAO

class CommitImportService(repoStatusDao: RepositoryStatusDAO, branchStateDao: BranchStateDAO, cache: BranchCommitsCache) extends Logging {

  def importRepoCommits(repository: Repository) {
    try {
      repository.pullChanges()
    } catch {
      // TODO: refactor a bit and  add info to user that remote repository is unavailable
      case e: Exception => logger.error("Cannot pull changes from upstream", e)
    }
    try {
      val loaded = repository.loadCommitsSince(branchStateDao.loadBranchesStateAsMap)
      cache.addCommits(loaded)
      updateRepoReadyStatus(repository)
    } catch {
      case e: Exception => {
        logger.error("Cannot import repository commits", e)
        updateRepoNotReadyStatus(repository, e.getMessage)
      }
    }
  }

  private def updateRepoNotReadyStatus(repository: Repository, errorMsg: String) {
    logger.debug(s"Saving repository-not-ready status data to DB with message: $errorMsg")
    val repoNotReadyStatus = RepositoryStatus.notReady(repository.repoName, Some(errorMsg))
    repoStatusDao.updateRepoStatus(repoNotReadyStatus)
  }

  private def updateRepoReadyStatus(repository: Repository) {
    logger.debug(s"Saving repository-ready status data to DB")
    val repoReadyStatus = RepositoryStatus.ready(repository.repoName)
    repoStatusDao.updateRepoStatus(repoReadyStatus)
  }

}