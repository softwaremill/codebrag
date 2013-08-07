package com.softwaremill.codebrag.service.config

import com.typesafe.config.Config

trait RepositoryConfig {
  def rootConfig: Config

  private lazy val repositoryConfig = rootConfig.getConfig("repository")

  lazy val repositoryType = repositoryConfig.getString("type")

  private lazy val githubRepositoryConfig = repositoryConfig.getConfig("github")

  lazy val githubRepositoryOwner = githubRepositoryConfig.getString("owner")
  lazy val githubRepositoryName = githubRepositoryConfig.getString("name")
  lazy val githubRepositoryBranch = githubRepositoryConfig.getString("branch")
  lazy val githubRepositorySyncUserLogin = githubRepositoryConfig.getString("sync-user-login")

  private lazy val gitHttpsRepositoryConfig = repositoryConfig.getConfig("git-https")

  lazy val gitHttpsRepositoryName = gitHttpsRepositoryConfig.getString("name")
  lazy val gitHttpsRepositoryUri = gitHttpsRepositoryConfig.getString("uri")
  lazy val gitHttpsRepositoryBranch = gitHttpsRepositoryConfig.getString("branch")
  lazy val gitHttpsRepositoryUsername = gitHttpsRepositoryConfig.getString("username")
  lazy val gitHttpsRepositoryPassword = gitHttpsRepositoryConfig.getString("password")

  private lazy val gitSshRepositoryConfig = repositoryConfig.getConfig("git-ssh")

  lazy val gitSshRepositoryName = gitSshRepositoryConfig.getString("name")
  lazy val gitSshRepositoryUri = gitSshRepositoryConfig.getString("uri")
  lazy val gitSshRepositoryBranch = gitSshRepositoryConfig.getString("branch")
  lazy val gitSshPassphrase = gitSshRepositoryConfig.getString("passphrase")
}
