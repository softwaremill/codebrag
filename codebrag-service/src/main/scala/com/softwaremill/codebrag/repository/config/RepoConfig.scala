package com.softwaremill.codebrag.repository.config

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.config.RepositoryConfig

class RepoConfig(repositoryConfig: RepositoryConfig) extends Logging {

  lazy val repoLocation: String = ???
  lazy val repoName: String = ???

  lazy val repoType: String = ??? // TODO will detect repo type

  lazy val credentials: Option[RepoCredentials] = {
    val passphraseCredentials = repositoryConfig.passphrase.map(PassphraseCredentials(_))
    val userPassCredentials = repositoryConfig.username.map { username =>
      val password = repositoryConfig.password.getOrElse("")
      UserPassCredentials(username, password)
    }
    passphraseCredentials.orElse(userPassCredentials)
  }

}