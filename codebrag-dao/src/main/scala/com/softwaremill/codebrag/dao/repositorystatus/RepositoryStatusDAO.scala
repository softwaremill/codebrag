package com.softwaremill.codebrag.dao.repositorystatus

import com.softwaremill.codebrag.domain.RepositoryStatus

trait RepositoryStatusDAO {
  def updateRepoStatus(newStatus: RepositoryStatus)
  def getRepoStatus(repoName: String): Option[RepositoryStatus]
}