package com.softwaremill.codebrag

import activities.AddCommentActivity
import common.{ObjectIdGenerator, IdGenerator}
import com.softwaremill.codebrag.dao.reporting.{MongoFollowupFinder, MongoCommentFinder, MongoCommitFinder}
import dao._
import rest.CodebragSwagger
import service.comments.CommentService
import com.softwaremill.codebrag.service.diff.{DiffWithCommentsService, DiffService}
import service.followups.FollowupService
import service.github._
import service.user.Authenticator
import pl.softwaremill.common.util.time.RealTimeClock
import com.softwaremill.codebrag.service.github.jgit.JgitGitHubCommitImportServiceFactory


trait Beans {
  implicit lazy val clock = new RealTimeClock
  implicit lazy val idGenerator: IdGenerator = new ObjectIdGenerator

  lazy val authenticator = new Authenticator(userDao)
  lazy val userDao = new MongoUserDAO
  lazy val commitInfoDao = new MongoCommitInfoDAO
  lazy val followupDao = new MongoFollowupDAO
  lazy val commitListFinder = new MongoCommitFinder
  lazy val commentListFinder = new MongoCommentFinder(userDao)
  lazy val swagger = new CodebragSwagger
  lazy val ghService = new GitHubAuthService
  lazy val commentDao = new MongoCommitCommentDAO
  lazy val commentService = new CommentService(commentDao)
  lazy val githubClientProvider = new GitHubClientProvider(userDao)
  lazy val converter = new GitHubCommitInfoConverter()
  lazy val commitReviewTaskDao = new MongoCommitReviewTaskDAO
  lazy val reviewTaskGenerator = new CommitReviewTaskGenerator(userDao, commitReviewTaskDao)
  lazy val importerFactory = new JgitGitHubCommitImportServiceFactory(commitInfoDao, reviewTaskGenerator, userDao)
  lazy val followupService = new FollowupService(followupDao, commitInfoDao, commentDao, userDao)
  lazy val followupFinder = new MongoFollowupFinder
  lazy val commentActivity = new AddCommentActivity(commentService, followupService)

  lazy val diffWithCommentsService = new DiffWithCommentsService(commitListFinder, commentListFinder, new DiffService(commitInfoDao))
}
