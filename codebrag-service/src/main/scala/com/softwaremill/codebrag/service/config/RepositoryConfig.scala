package com.softwaremill.codebrag.service.config

import com.typesafe.config.Config

trait RepositoryConfig {
  def rootConfig: Config

  lazy val repositoryOwner: String = rootConfig.getString("repository.owner")
  lazy val repositoryName: String = rootConfig.getString("repository.name")
}
