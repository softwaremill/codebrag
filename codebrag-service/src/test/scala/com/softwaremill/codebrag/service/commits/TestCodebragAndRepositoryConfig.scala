package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.service.config.{RepositoryConfig, CodebragConfig}
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}

object TestCodebragAndRepositoryConfig extends CodebragConfig with RepositoryConfig {
  override lazy val localGitStoragePath = ""

  def rootConfig = ???

  override lazy val repositoryType = GitSshRepoType.configRepoType
}
