package com.softwaremill.codebrag.dao.repositorystatus

import com.softwaremill.codebrag.domain.RepositoryStatus

trait RepositoryStatusDAO extends RepositorySnapshotDAO {
  def updateRepoStatus(newStatus: RepositoryStatus)
  def getRepoStatus(repoName: String): Option[RepositoryStatus]
}

/**
 * Temporary in-memory implementation
 * Don't want to fight with Slick right now
 */
trait RepositorySnapshotDAO {

  private val storage = new scala.collection.mutable.HashMap[String, String]()

  def storeBranchState(branchName: String, sha: String) {
    storage.put(branchName, sha)
  }

  def loadBranchState(branchName: String) = storage.get(branchName)

  def loadBranchesState = storage.toMap

}
