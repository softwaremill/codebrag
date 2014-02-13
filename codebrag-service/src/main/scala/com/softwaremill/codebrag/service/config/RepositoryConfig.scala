package com.softwaremill.codebrag.service.config

import com.typesafe.config.Config
import com.softwaremill.codebrag.repository.config.RepoConfig

trait RepositoryConfig {

  def rootConfig: Config
  private val repositoryConfigSection = rootConfig.getConfig("repository")

  val repositoryConfig = new RepoConfig(repositoryConfigSection)

  // for user/pass
  val username = getOptional("username")
  val password = getOptional("password")

  // for SSH
  val passphrase = getOptional("passphrase")

  private def getOptional(path: String, default: Option[String] = None) = {
    if(repositoryConfigSection.hasPath(path)) {
      Some(repositoryConfigSection.getString(path))
    } else {
      default
    }
  }
}