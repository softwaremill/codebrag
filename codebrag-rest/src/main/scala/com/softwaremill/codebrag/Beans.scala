package com.softwaremill.codebrag

import com.softwaremill.codebrag.activities.{CommitReviewActivity, AddCommentActivity}
import com.softwaremill.codebrag.common.{RealTimeClock, ObjectIdGenerator, IdGenerator}
import com.softwaremill.codebrag.dao.reporting._
import dao._
import com.softwaremill.codebrag.rest.CodebragSwagger
import com.softwaremill.codebrag.service.comments.{LikeValidator, UserReactionService}
import com.softwaremill.codebrag.service.diff.{DiffWithCommentsService, DiffService}
import com.softwaremill.codebrag.service.followups.{FollowupsGeneratorForReactionsPriorUserRegistration, WelcomeFollowupsGenerator, FollowupService}
import service.commits._
import com.softwaremill.codebrag.service.user._
import com.softwaremill.codebrag.service.events.akka.AkkaEventBus
import com.softwaremill.codebrag.service.actors.ActorSystemSupport
import com.softwaremill.codebrag.service.config._
import com.typesafe.config.ConfigFactory
import com.softwaremill.codebrag.usecase.{ChangeUserSettingsUseCase, UnlikeUseCase}
import com.softwaremill.codebrag.dao.finders.commit.{ReviewableCommitsListFinder, AllCommitsFinder}
import com.softwaremill.codebrag.service.invitations.{DefaultUniqueHashGenerator, InvitationService}
import com.softwaremill.codebrag.service.email.{EmailService, EmailScheduler}
import com.softwaremill.codebrag.service.notification.NotificationService
import com.softwaremill.codebrag.service.templates.TemplateEngine
import com.softwaremill.codebrag.dao.eventstream.EventDao
import com.softwaremill.codebrag.stats.{InstanceRunStatsSender, StatsHTTPRequestSender, StatsAggregator}
import com.softwaremill.codebrag.dao.user.{MongoInternalUserDAO, MongoUserDAO}
import com.softwaremill.codebrag.dao.mongo.MongoConfig
import com.softwaremill.codebrag.dao.commitinfo.MongoCommitInfoDAO
import com.softwaremill.codebrag.dao.reaction.MongoCommitCommentDAO

trait Beans extends ActorSystemSupport with CommitsModule with Finders with Daos {

  lazy val config = new MongoConfig with RepositoryConfig with GithubConfig with CodebragConfig with EmailConfig with CodebragStatsConfig {
    def rootConfig = ConfigFactory.load()
  }

  implicit lazy val clock = RealTimeClock
  implicit lazy val idGenerator: IdGenerator = new ObjectIdGenerator
  val self = this
  lazy val eventBus = new AkkaEventBus(actorSystem)
  lazy val swagger = new CodebragSwagger
  lazy val ghService = new GitHubAuthService(config)
  lazy val followupService = new FollowupService(followupDao, commitInfoDao, commentDao, userDao)
  lazy val likeValidator = new LikeValidator(commitInfoDao, likeDao, userDao)
  lazy val userReactionService = new UserReactionService(commentDao, likeDao, likeValidator, eventBus)
  lazy val repoStatusDao = new MongoRepositoryStatusDAO
  lazy val emailService = new EmailService(config)
  lazy val emailScheduler = new EmailScheduler(actorSystem, EmailScheduler.createActor(actorSystem, emailService))
  lazy val templateEngine = new TemplateEngine()
  lazy val invitationsService = new InvitationService(invitationDao, userDao, emailService, config, DefaultUniqueHashGenerator, templateEngine)
  lazy val notificationService = new NotificationService(emailScheduler, templateEngine, config, notificationCountFinder, clock)
  lazy val heartbeatStore = new MongoHeartbeatStore(clock)

  lazy val reviewTaskGenerator = new CommitReviewTaskGeneratorActions {
    val userDao = self.userDao
    val commitInfoDao = self.commitInfoDao
    val commitToReviewDao = self.commitReviewTaskDao
  }

  lazy val welcomeFollowupsGenerator = new WelcomeFollowupsGenerator(internalUserDao, commentDao, likeDao, followupDao, commitInfoDao, templateEngine)
  lazy val followupGeneratorForPriorReactions = new FollowupsGeneratorForReactionsPriorUserRegistration(commentDao, likeDao, followupDao, commitInfoDao, config)

  lazy val authenticator = new UserPasswordAuthenticator(userDao, eventBus, reviewTaskGenerator)
  lazy val emptyGithubAuthenticator = new GitHubEmptyAuthenticator(userDao)
  lazy val commentActivity = new AddCommentActivity(userReactionService, followupService, eventBus)

  lazy val commitReviewActivity = new CommitReviewActivity(commitReviewTaskDao, commitInfoDao, eventBus)

  lazy val newUserAdder = new NewUserAdder(userDao, eventBus, reviewTaskGenerator, followupGeneratorForPriorReactions, welcomeFollowupsGenerator)
  lazy val registerService = new RegisterService(userDao, newUserAdder, invitationsService, notificationService)

  lazy val diffWithCommentsService = new DiffWithCommentsService(allCommitsFinder, reactionFinder, new DiffService(commitInfoDao))

  lazy val statsAggregator = new StatsAggregator(statsFinder, instanceSettingsDao)

  lazy val unlikeUseCaseFactory = new UnlikeUseCase(likeValidator, userReactionService)
  lazy val changeUserSettingsUseCase = new ChangeUserSettingsUseCase(userDao)

  lazy val instanceSettings = instanceSettingsDao.readOrCreate match {
    case Left(error) => throw new RuntimeException(s"Cannot properly initialise Instance settings: $error")
    case Right(instance) => instance
  }

  lazy val statsHTTPRequestSender = new StatsHTTPRequestSender(config)
  lazy val instanceRunStatsSender = new InstanceRunStatsSender(statsHTTPRequestSender)

}

trait Daos {

  lazy val userDao = new MongoUserDAO
  lazy val internalUserDao = new MongoInternalUserDAO

  lazy val commitInfoDao = new MongoCommitInfoDAO
  lazy val followupDao = new MongoFollowupDAO
  lazy val followupWithReactionsDao = new MongoFollowupWithReactionsDAO(commentDao, likeDao)

  lazy val likeDao = new MongoLikeDAO
  lazy val commentDao = new MongoCommitCommentDAO

  lazy val commitReviewTaskDao = new MongoCommitReviewTaskDAO

  lazy val invitationDao = new MongoInvitationDAO

  lazy val instanceSettingsDao = new FileBasedInstanceSettingsDAO

  lazy val eventDao = new EventDao

}

trait Finders {

  lazy val notificationCountFinder = new MongoNotificationCountFinder

  lazy val reactionFinder = new MongoReactionFinder

  lazy val allCommitsFinder = new AllCommitsFinder
  lazy val reviewableCommitsFinder = new ReviewableCommitsListFinder

  lazy val followupFinder = new MongoFollowupFinder

  lazy val statsFinder = new StatsEventsFinder

}