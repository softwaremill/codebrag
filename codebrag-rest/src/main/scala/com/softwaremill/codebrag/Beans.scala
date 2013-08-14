package com.softwaremill.codebrag

import activities.AddCommentActivity
import com.softwaremill.codebrag.common.{ObjectIdGenerator, IdGenerator}
import com.softwaremill.codebrag.dao.reporting._
import dao._
import com.softwaremill.codebrag.rest.CodebragSwagger
import com.softwaremill.codebrag.service.comments.{LikeValidator, UserReactionService}
import com.softwaremill.codebrag.service.diff.{DiffWithCommentsService, DiffService}
import service.followups.FollowupService
import service.commits._
import com.softwaremill.codebrag.service.user._
import pl.softwaremill.common.util.time.RealTimeClock
import com.softwaremill.codebrag.service.events.akka.AkkaEventBus
import com.softwaremill.codebrag.service.actors.ActorSystemSupport
import com.softwaremill.codebrag.service.config.{CodebragConfig, RepositoryConfig, GithubConfig}
import com.typesafe.config.ConfigFactory
import com.softwaremill.codebrag.usecase.UnlikeUseCaseFactory
import com.softwaremill.codebrag.dao.finders.commit.{ReviewableCommitsListFinder, AllCommitsFinder}

trait Beans extends ActorSystemSupport with CommitsModule with Finders with Daos{

  lazy val config = new MongoConfig with RepositoryConfig with GithubConfig with CodebragConfig {
    def rootConfig = ConfigFactory.load()
  }

  implicit lazy val clock = new RealTimeClock
  implicit lazy val idGenerator: IdGenerator = new ObjectIdGenerator
  val self = this
  lazy val eventBus = new AkkaEventBus(actorSystem)
  lazy val swagger = new CodebragSwagger
  lazy val ghService = new GitHubAuthService(config)
  lazy val followupService = new FollowupService(followupDao, commitInfoDao, commentDao, userDao)
  lazy val likeValidator = new LikeValidator(commitInfoDao, likeDao, userDao)
  lazy val userReactionService = new UserReactionService(commentDao, likeDao, likeValidator, eventBus)
  lazy val repoHeadStore = new MongoRepositoryHeadStore


  lazy val reviewTaskGenerator = new CommitReviewTaskGeneratorActions {
      val userDao = self.userDao
      val commitInfoDao = self.commitInfoDao
      val commitToReviewDao = self.commitReviewTaskDao
    }

  lazy val authenticator = new UserPasswordAuthenticator(userDao, eventBus, reviewTaskGenerator)
  lazy val emptyGithubAuthenticator = new GitHubEmptyAuthenticator(userDao)
  lazy val commentActivity = new AddCommentActivity(userReactionService, followupService)

  lazy val newUserAdder = new NewUserAdder(userDao, eventBus, reviewTaskGenerator)
  lazy val registerService = new RegisterService(userDao, newUserAdder)

  lazy val diffWithCommentsService = new DiffWithCommentsService(allCommitsFinder, reactionFinder, new DiffService(commitInfoDao))

  lazy val unlikeUseCaseFactory = new UnlikeUseCaseFactory(likeValidator, userReactionService)
}

trait Daos {

  lazy val userDao = new MongoUserDAO
  lazy val commitInfoDao = new MongoCommitInfoDAO
  lazy val followupDao = new MongoFollowupDAO
  lazy val followupWithReactionsDao = new MongoFollowupWithReactionsDAO(commentDao, likeDao)

  lazy val likeDao = new MongoLikeDAO
  lazy val commentDao = new MongoCommitCommentDAO

  lazy val commitReviewTaskDao = new MongoCommitReviewTaskDAO

}

trait Finders {

  lazy val notificationCountFinder = new MongoNotificationCountFinder

  lazy val reactionFinder = new MongoReactionFinder

  lazy val allCommitsFinder = new AllCommitsFinder
  lazy val reviewableCommitsFinder = new ReviewableCommitsListFinder

  lazy val followupFinder = new MongoFollowupFinder

}