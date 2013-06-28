package com.softwaremill.codebrag

import activities.AddCommentActivity
import com.softwaremill.codebrag.common.{ObjectIdGenerator, IdGenerator}
import com.softwaremill.codebrag.dao.reporting._
import dao._
import rest.CodebragSwagger
import com.softwaremill.codebrag.service.comments.{LikeValidator, UserReactionService}
import com.softwaremill.codebrag.service.diff.{DiffWithCommentsService, DiffService}
import service.followups.FollowupService
import service.commits._
import com.softwaremill.codebrag.service.user.{RegisterService, GitHubAuthService, GitHubEmptyAuthenticator, UserPasswordAuthenticator}
import pl.softwaremill.common.util.time.RealTimeClock
import com.softwaremill.codebrag.service.events.akka.AkkaEventBus
import com.softwaremill.codebrag.service.actors.ActorSystemSupport
import com.softwaremill.codebrag.service.config.{CodebragConfig, RepositoryConfig, GithubConfig}
import com.typesafe.config.ConfigFactory

trait Beans extends ActorSystemSupport with CommitsModule {

  lazy val config = new MongoConfig with RepositoryConfig with GithubConfig with CodebragConfig {
    def rootConfig = ConfigFactory.load()
  }

  implicit lazy val clock = new RealTimeClock
  implicit lazy val idGenerator: IdGenerator = new ObjectIdGenerator
  val self = this
  lazy val eventBus = new AkkaEventBus(actorSystem)
  lazy val userDao = new MongoUserDAO
  lazy val commitInfoDao = new MongoCommitInfoDAO
  lazy val followupDao = new MongoFollowupDAO
  lazy val commitListFinder = new MongoCommitWithAuthorDetailsFinder(new MongoCommitFinder)
  lazy val reactionFinder = new ReactionFinder
  lazy val swagger = new CodebragSwagger
  lazy val ghService = new GitHubAuthService(config)
  lazy val commentDao = new MongoCommitCommentDAO
  lazy val notificationCountFinder = new MongoNotificationCountFinder
  lazy val commitReviewTaskDao = new MongoCommitReviewTaskDAO
  lazy val followupService = new FollowupService(followupDao, commitInfoDao, commentDao, userDao)
  lazy val likeDao = new MongoLikeDAO
  lazy val likeValidator = new LikeValidator(commitInfoDao, likeDao, userDao)
  lazy val userReactionService = new UserReactionService(commentDao, likeDao, likeValidator, eventBus)

  lazy val reviewTaskGenerator = new CommitReviewTaskGeneratorActions {
      val userDao = self.userDao
      val commitInfoDao = self.commitInfoDao
      val commitToReviewDao = self.commitReviewTaskDao
    }

  lazy val authenticator = new UserPasswordAuthenticator(userDao, eventBus, reviewTaskGenerator)
  lazy val emptyGithubAuthenticator = new GitHubEmptyAuthenticator(userDao)
  lazy val followupFinder = new MongoFollowupFinder
  lazy val commentActivity = new AddCommentActivity(userReactionService, followupService)

  lazy val registerService = new RegisterService()

  lazy val diffWithCommentsService = new DiffWithCommentsService(commitListFinder, reactionFinder, new DiffService(commitInfoDao))
}
