package com.softwaremill.codebrag.service.config

import com.typesafe.config.Config
import com.softwaremill.codebrag.repository.config.RepoConfig

trait RepositoryConfig {

  def rootConfig: Config
  private val repositoryConfigSection = rootConfig.getConfig("repository")

  val repositoryConfig = new RepoConfig(repositoryConfigSection)

}