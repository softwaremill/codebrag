package com.softwaremill.codebrag

import dao.{MongoCommitInfoDAO, CommitInfoDAO, MongoUserDAO}
import rest.CodebragSwagger
import service.user.Authenticator

trait Beans {

  lazy val authenticator = new Authenticator(userDao)
  lazy val userDao = new MongoUserDAO
  lazy val commitInfoDao = new MongoCommitInfoDAO
  lazy val swagger = new CodebragSwagger
}
