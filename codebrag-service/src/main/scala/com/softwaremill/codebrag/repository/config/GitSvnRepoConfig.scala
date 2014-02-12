package com.softwaremill.codebrag.repository.config

import com.typesafe.config.Config

class GitSvnRepoConfig(val repositoryConfig: Config) extends RepoConfig {

  lazy val credentials: Option[UserPassCredentials] = {
    if(repositoryConfig.hasPath("username") && repositoryConfig.hasPath("password")) {
      logger.info("User/password found in configuration - using as repo credentials")
      val username = repositoryConfig.getString("username")
      val password = repositoryConfig.getString("password")
      Some(UserPassCredentials(username, password))
    } else {
      logger.info("No credentials found in configuration - using no credentials")
      None
    }
  }

}
