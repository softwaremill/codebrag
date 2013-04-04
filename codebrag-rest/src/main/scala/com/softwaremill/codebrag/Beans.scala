package com.softwaremill.codebrag

import activities.CommentActivity
import common.{ObjectIdGenerator, IdGenerator}
import dao.reporting.{MongoCommentListFinder, MongoCommitListFinder}
import dao._
import rest.CodebragSwagger
import service.comments.CommentService
import service.diff.DiffService
import service.followups.FollowupService
import service.github._
import service.user.Authenticator
import pl.softwaremill.common.util.time.RealTimeClock


trait Beans {
  implicit lazy val clock = new RealTimeClock
  implicit lazy val idGenerator: IdGenerator = new ObjectIdGenerator

  lazy val authenticator = new Authenticator(userDao)
  lazy val userDao = new MongoUserDAO
  lazy val reviewDao = new MongoCommitReviewDAO
  lazy val commitInfoDao = new MongoCommitInfoDAO
  lazy val followupDao = new MongoFollowupDAO
  lazy val commitReviewDao = new MongoCommitReviewDAO
  lazy val commitListFinder = new MongoCommitListFinder
  lazy val commentListFinder = new MongoCommentListFinder
  lazy val swagger = new CodebragSwagger
  lazy val ghService = new GitHubAuthService
  lazy val commentService = new CommentService(reviewDao, userDao)
  lazy val diffService = new DiffService(commitInfoDao)
  lazy val githubClientProvider = new GitHubClientProvider(userDao)
  lazy val converter = new GitHubCommitInfoConverter()
  lazy val importerFactory = new GitHubCommitImportServiceFactory(githubClientProvider, converter, commitInfoDao)
  lazy val followupService = new FollowupService(followupDao, commitInfoDao, commitReviewDao)

  lazy val commentActivity = new CommentActivity(commentService, followupService)
}
