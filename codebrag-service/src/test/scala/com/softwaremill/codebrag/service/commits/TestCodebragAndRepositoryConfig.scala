package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.service.config.{RepositoryConfig, CodebragConfig}
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}

object TestCodebragAndRepositoryConfig extends CodebragConfig with RepositoryConfig {
  override lazy val localGitStoragePath = ""

  def rootConfig = ConfigFactory.parseString("repository.type=git-ssh, repository.git-ssh.name=codebrag, repository.git-ssh.uri=\"git@github.com:/softwaremill/codebrag.git\", repository.git-ssh.passphrase=abc, repository.git-ssh.branch=refs/heads/master")

}
