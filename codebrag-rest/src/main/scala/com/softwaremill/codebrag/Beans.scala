package com.softwaremill.codebrag

import common.{UuidGenerator, IdGenerator}
import dao.reporting.MongoCommitListFinder
import dao.{MongoCommitInfoDAO, MongoUserDAO}
import rest.CodebragSwagger
import service.comments.CommentService
import service.github.GitHubAuthService
import service.user.Authenticator
import pl.softwaremill.common.util.time.RealTimeClock


trait Beans {
  implicit lazy val clock = new RealTimeClock
  implicit lazy val idGenerator: IdGenerator = new UuidGenerator

  lazy val authenticator = new Authenticator(userDao)
  lazy val userDao = new MongoUserDAO
  lazy val commitInfoDao = new MongoCommitInfoDAO
  lazy val commitListFinder = new MongoCommitListFinder
  lazy val swagger = new CodebragSwagger
  lazy val ghService = new GitHubAuthService
  lazy val commentService = new CommentService(commitInfoDao, userDao)
}
