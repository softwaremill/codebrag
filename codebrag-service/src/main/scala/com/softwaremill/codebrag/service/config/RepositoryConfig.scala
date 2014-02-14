package com.softwaremill.codebrag.service.config

import com.typesafe.config.Config

/*
  This is repository section from config file. It looks like below:
  It can have either username/password keys or passphrase key configured.
  Also it can have repositories directory set. By default it is set to "./repos"

  repository {
    username = "johndoe"
    password = "secret"

    repos-root = "/your/repos/root"
  }

 */

trait RepositoryConfig extends ConfigWithDefault {

  def rootConfig: Config

  // directory containing cloned repo dir
  val repositoriesRoot = getString("repository.repos-root", "./repos")

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