package com.softwaremill.codebrag.service.config

import com.typesafe.config.Config

trait RepositoryConfig {

  def rootConfig: Config

  // for user/pass
  val username = getOptional("username")
  val password = getOptional("password")

  // for SSH
  val passphrase = getOptional("passphrase")

  private def getOptional(path: String, default: Option[String] = None) = {
    val repositoryConfigSection = rootConfig.getConfig("repository")
    if(repositoryConfigSection.hasPath(path)) {
      Some(repositoryConfigSection.getString(path))
    } else {
      default
    }
  }
}