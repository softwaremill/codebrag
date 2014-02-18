package com.softwaremill.codebrag.service.config

import com.typesafe.config.Config
import com.softwaremill.codebrag.common.config.ConfigWithDefault

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
  val username = getOptionalString("repository.username")
  val password = getOptionalString("repository.password")

  // for SSH
  val passphrase = getOptionalString("repository.passphrase")

}