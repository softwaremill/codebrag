package com.softwaremill.codebrag.domain

case class RepositoryStatus(repositoryName: String, ready: Boolean, error: Option[String])

object RepositoryStatus extends ((String, Boolean, Option[String]) => RepositoryStatus) {
  def ready(repoName: String) = new RepositoryStatus(repoName, true, None)
  def notReady(repoName: String, error: Option[String] = None) = new RepositoryStatus(repoName, false, error)
}


