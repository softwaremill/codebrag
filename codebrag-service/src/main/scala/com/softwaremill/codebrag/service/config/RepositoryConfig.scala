package com.softwaremill.codebrag.service.config

import com.typesafe.config.Config

trait RepositoryConfig {
  def rootConfig: Config

  private lazy val repositoryConfig = rootConfig.getConfig("repository")

  lazy val repositoryType = repositoryConfig.getString("type")

  private lazy val githubRepositoryConfig = repositoryConfig.getConfig("github")

  lazy val githubRepositoryOwner = githubRepositoryConfig.getString("owner")
  lazy val githubRepositoryName = githubRepositoryConfig.getString("name")
  lazy val githubRepositorySyncUserLogin = githubRepositoryConfig.getString("sync-user-login")

  private lazy val gitRepositoryConfig = repositoryConfig.getConfig("git")

  lazy val gitRepositoryName = gitRepositoryConfig.getString("name")
  lazy val gitRepositoryUri = gitRepositoryConfig.getString("uri")
  lazy val gitRepositoryUsername = gitRepositoryConfig.getString("username")
  lazy val gitRepositoryPassword = gitRepositoryConfig.getString("password")
}
