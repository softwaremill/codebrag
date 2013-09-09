package com.softwaremill.codebrag.service.config

trait CodebragConfig extends ConfigWithDefault {

  lazy val localGitStoragePath: String = rootConfig.getString("codebrag.local-git-storage-path")
  lazy val debugServicesLogin: String = rootConfig.getString("codebrag.debug-services-login")
  lazy val debugServicesPassword: String = rootConfig.getString("codebrag.debug-services-password")
  lazy val demo: Boolean = getBoolean("codebrag.demo", default = false)
  lazy val applicationUrl: String = rootConfig.getString("codebrag.applicationUrl")
}


