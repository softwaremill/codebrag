package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.service.config.{RepositoryConfig, CodebragConfig}

object TestCodebragAndRepositoryConfig extends CodebragConfig with RepositoryConfig {
  override lazy val localGitStoragePath = ""

  def rootConfig = ???
}
