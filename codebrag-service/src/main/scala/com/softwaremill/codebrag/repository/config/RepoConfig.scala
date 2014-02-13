package com.softwaremill.codebrag.repository.config

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.config.RepositoryConfig

class RepoConfig(val repositoryConfig: RepositoryConfig) extends RepoDiscovery with Logging {

  lazy val credentials: Option[RepoCredentials] = {
    val passphraseCredentials = repositoryConfig.passphrase.map(PassphraseCredentials(_))
    val userPassCredentials = repositoryConfig.username.map { username =>
      val password = repositoryConfig.password.getOrElse("")
      UserPassCredentials(username, password)
    }
    passphraseCredentials.orElse(userPassCredentials)
  }

}