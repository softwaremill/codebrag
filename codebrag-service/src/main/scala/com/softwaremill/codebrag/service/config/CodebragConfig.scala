package com.softwaremill.codebrag.service.config

import com.typesafe.config.Config

trait CodebragConfig {
  def rootConfig: Config

  lazy val codebragLocalGitPath: String = rootConfig.getString("codebrag.local-git-path")
  lazy val codebragSyncUserLogin: String = rootConfig.getString("codebrag.sync-user-login")
}
