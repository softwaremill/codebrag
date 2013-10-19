package com.softwaremill.codebrag.domain

case class RepositoryStatus(repositoryName: String, headId: Option[String], ready: Boolean, error: Option[String]) {

  def withHeadId(newHeadId: String) = this.copy(headId = Some(newHeadId))

}

object RepositoryStatus {
  def ready(repoName: String) = new RepositoryStatus(repoName, None, true, None)
  def notReady(repoName: String, error: Option[String] = None) = new RepositoryStatus(repoName, None, false, error)
}


