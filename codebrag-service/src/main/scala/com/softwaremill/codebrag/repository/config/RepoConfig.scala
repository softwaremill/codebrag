package com.softwaremill.codebrag.repository.config

import com.typesafe.scalalogging.slf4j.Logging
import com.typesafe.config.Config
import java.io.File

class RepoConfig(repositoryConfig: Config) extends Logging {

  lazy val repoLocation = repositoryConfig.getString("location")
  lazy val repoName = repoLocation.split(File.separator).last

  lazy val repoType: String = ??? // TODO will detect repo type

  lazy val credentials: Option[RepoCredentials] = {
    checkCredentialsConfig
    if(repositoryConfig.hasPath("passphrase")) {
      logger.info("Passphrase found in configuration - using as repo credentials")
      val passphrase = repositoryConfig.getString("passphrase")
      Some(PassphraseCredentials(passphrase))
    } else if(repositoryConfig.hasPath("username") && repositoryConfig.hasPath("password")) {
      logger.info("User/password found in configuration - using as repo credentials")
      val username = repositoryConfig.getString("username")
      val password = repositoryConfig.getString("password")
      Some(UserPassCredentials(username, password))
    } else {
      logger.info("No credentials found in configuration - using no credentials")
      None
    }
  }

  private def checkCredentialsConfig {
    val keys = List("username", "password", "passphrase")
    val allExist = (keys.filter(repositoryConfig.hasPath(_)).size == keys.size)
    if(allExist) throw new RuntimeException("Either username/password or passphrase (not both) can be defined in repository config!")
  }

}
