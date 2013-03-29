package com.softwaremill.codebrag

import common.{ObjectIdGenerator, IdGenerator}
import dao.reporting.MongoCommitListFinder
import dao.{MongoCommitReviewDAO, CommitReviewDAO, MongoCommitInfoDAO, MongoUserDAO}
import rest.CodebragSwagger
import service.comments.CommentService
import service.diff.DiffService
import service.github.GitHubAuthService
import service.user.Authenticator
import pl.softwaremill.common.util.time.RealTimeClock


trait Beans {
  implicit lazy val clock = new RealTimeClock
  implicit lazy val idGenerator: IdGenerator = new ObjectIdGenerator

  lazy val authenticator = new Authenticator(userDao)
  lazy val userDao = new MongoUserDAO
  lazy val reviewDao = new MongoCommitReviewDAO
  lazy val commitInfoDao = new MongoCommitInfoDAO
  lazy val commitListFinder = new MongoCommitListFinder
  lazy val swagger = new CodebragSwagger
  lazy val ghService = new GitHubAuthService
  lazy val commentService = new CommentService(reviewDao, userDao)
  lazy val diffService = new DiffService(commitInfoDao)
}
