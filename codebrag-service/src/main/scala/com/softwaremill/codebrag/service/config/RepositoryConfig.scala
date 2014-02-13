package com.softwaremill.codebrag.service.config

import com.typesafe.config.Config

trait RepositoryConfig extends ConfigWithDefault {

  def rootConfig: Config

  // directory containing cloned repo dir
  val repositoriesRoot = getString("repository.repos-root", ".")

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