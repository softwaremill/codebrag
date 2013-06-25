package com.softwaremill.codebrag.service.github

import com.softwaremill.codebrag.service.config.CodebragConfig

object TestCodebragConfig extends CodebragConfig {
  override lazy val localGitStoragePath = ""

  def rootConfig = ???
}
