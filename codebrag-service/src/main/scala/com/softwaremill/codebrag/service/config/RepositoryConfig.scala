package com.softwaremill.codebrag.service.config

import com.typesafe.config.Config

trait RepositoryConfig extends ConfigWithDefault {

  def rootConfig: Config

  // directory containing cloned repo dir
  val repositoriesRoot = getString("repository.repos-root", "repos")

  // for user/pass
  val username = getOptional("repository.username")
  val password = getOptional("repository.password")

  // for SSH
  val passphrase = getOptional("repository.passphrase")

  private def getOptional(fullPath: String, default: Option[String] = None) = {
    if(rootConfig.hasPath(fullPath)) {
      Some(rootConfig.getString(fullPath))
    } else {
      default
    }
  }

}