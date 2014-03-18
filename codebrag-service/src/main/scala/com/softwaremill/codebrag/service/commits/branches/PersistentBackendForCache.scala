package com.softwaremill.codebrag.service.commits.branches

import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.{BranchState, MultibranchLoadCommitsResult}
import com.softwaremill.codebrag.dao.branchsnapshot.BranchStateDAO

class PersistentBackendForCache(commitInfoDao: CommitInfoDAO, branchStateDao: BranchStateDAO) extends Logging {

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
      val state = BranchState(branch.branchName, branch.currentBranchSHA)
      branchStateDao.storeBranchState(state)
      logger.debug(s"Persisted SHA ${branch.currentBranchSHA} for ${branch.branchName}")
    }
  }

  def loadBranchesState(): Map[String, String] = {
    logger.debug("Loading repo state from persistent storage")
    branchStateDao.loadBranchesStateAsMap
  }

}