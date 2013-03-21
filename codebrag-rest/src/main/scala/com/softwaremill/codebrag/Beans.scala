package com.softwaremill.codebrag

import dao.{MongoCommitInfoDAO, MongoUserDAO}
import rest.CodebragSwagger
import service.github.GitHubAuthService
import service.user.Authenticator

trait Beans {

  lazy val authenticator = new Authenticator(userDao)
  lazy val userDao = new MongoUserDAO
  lazy val commitInfoDao = new MongoCommitInfoDAO
  lazy val swagger = new CodebragSwagger
  lazy val ghService = new GitHubAuthService
}
