package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.service.config.CodebragConfig

object TestCodebragConfig extends CodebragConfig {
  override lazy val localGitStoragePath = ""

  def rootConfig = ???
}
