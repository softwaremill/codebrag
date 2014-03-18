package com.softwaremill.codebrag.service.commits.branches

import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.MultibranchLoadCommitsResult

class PersistentBackendForCache(commitInfoDao: CommitInfoDAO, repoStatusDao: RepositoryStatusDAO) extends Logging {

  def persist(loadResult: MultibranchLoadCommitsResult) {
    persistUniqueCommits(loadResult)
    persistBranchesState(loadResult)
  }


  private def persistUniqueCommits(loadResult: MultibranchLoadCommitsResult) {
    val uniqueCommits = loadResult.uniqueCommits
    logger.debug(s"Persisting cache changes: ${uniqueCommits.size} commits")
    uniqueCommits.foreach {
      c => commitInfoDao.storeCommit(c)
    }
  }

  private def persistBranchesState(loadResult: MultibranchLoadCommitsResult) {
    loadResult.commits.foreach { branch =>
      repoStatusDao.storeBranchState(branch.branchName, branch.currentBranchSHA)
      logger.debug(s"Persisted SHA ${branch.currentBranchSHA} for ${branch.branchName}")
    }
  }

  def loadBranchesState(): Map[String, String] = {
    logger.debug("Loading repo state from persistent storage")
    repoStatusDao.loadBranchesState
  }

}