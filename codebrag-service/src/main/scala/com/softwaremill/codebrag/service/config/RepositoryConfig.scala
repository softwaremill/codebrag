package com.softwaremill.codebrag.service.config

import com.typesafe.config.Config
import com.softwaremill.codebrag.repository.config.{GitSvnRepoConfig, GitRepoConfig}

trait RepositoryConfig {

  def rootConfig: Config
  private val repositoryConfigSection = rootConfig.getConfig("repository")

  val repositoryConfig = repositoryConfigSection.getString("type") match {
    case "git" => new GitRepoConfig(repositoryConfigSection)
    case "git-svn" => new GitSvnRepoConfig(repositoryConfigSection)
  }

}