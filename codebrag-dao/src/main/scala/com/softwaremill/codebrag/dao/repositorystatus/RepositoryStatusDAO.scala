package com.softwaremill.codebrag.dao.repositorystatus

import com.softwaremill.codebrag.domain.RepositoryStatus

trait RepositoryStatusDAO {
  def updateRepoStatus(newStatus: RepositoryStatus)
  def update(repoName: String, newSha: String) = updateRepoStatus(RepositoryStatus.ready(repoName).withHeadId(newSha))

  def get(repoName: String): Option[String]
  def getRepoStatus(repoName: String): Option[RepositoryStatus]
}
