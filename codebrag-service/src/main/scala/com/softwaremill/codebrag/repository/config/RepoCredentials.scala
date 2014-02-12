package com.softwaremill.codebrag.repository.config

trait RepoCredentials
case class UserPassCredentials(user: String, pass: String) extends RepoCredentials
case class PassphraseCredentials(phrase: String) extends RepoCredentials
