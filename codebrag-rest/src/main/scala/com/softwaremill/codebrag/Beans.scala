package com.softwaremill.codebrag

import com.softwaremill.codebrag.activities._
import com.softwaremill.codebrag.common.{RealTimeClock, ObjectIdGenerator, IdGenerator}
import com.softwaremill.codebrag.rest.CodebragSwagger
import com.softwaremill.codebrag.service.comments.{LikeValidator, UserReactionService}
import com.softwaremill.codebrag.service.diff.{DiffWithCommentsService, DiffService}
import com.softwaremill.codebrag.service.followups.{FollowupsGeneratorForReactionsPriorUserRegistration, WelcomeFollowupsGenerator, FollowupService}
import service.commits._
import com.softwaremill.codebrag.service.user._
import com.softwaremill.codebrag.service.events.akka.AkkaEventBus
import com.softwaremill.codebrag.service.actors.ActorSystemSupport
import com.softwaremill.codebrag.service.invitations.{DefaultUniqueHashGenerator, InvitationService}
import com.softwaremill.codebrag.service.email.{EmailService, EmailScheduler}
import com.softwaremill.codebrag.service.notification.NotificationService
import com.softwaremill.codebrag.service.templates.TemplateEngine
import com.softwaremill.codebrag.stats.{InstanceRunStatsSender, StatsHTTPRequestSender, StatsAggregator}
import com.softwaremill.codebrag.dao.Daos
import com.softwaremill.codebrag.repository.Repository
import com.softwaremill.codebrag.cache.{RepositoriesCache, UserReviewedCommitsCache, PersistentBackendForCache, RepositoryCache}
import com.softwaremill.codebrag.licence.LicenceService
import com.softwaremill.codebrag.instance.InstanceParamsService
import com.softwaremill.codebrag.activities.finders.toreview.{ToReviewCommitsViewBuilder, ToReviewBranchCommitsFilter, ToReviewCommitsFinder}
import com.softwaremill.codebrag.activities.finders.all.{AllCommitsViewBuilder, AllCommitsFinder}

trait Beans extends ActorSystemSupport with CommitsModule with Daos {

  def config: AllConfig
  def repository: Repository

  implicit lazy val clock = RealTimeClock
  implicit lazy val idGenerator: IdGenerator = new ObjectIdGenerator
  lazy val self = this
  lazy val eventBus = new AkkaEventBus(actorSystem)
  lazy val swagger = new CodebragSwagger
  lazy val ghService = new GitHubAuthService(config)
  lazy val followupService = new FollowupService(followupDao, commitInfoDao, commentDao, userDao)
  lazy val likeValidator = new LikeValidator(commitInfoDao, likeDao, userDao)
  lazy val userReactionService = new UserReactionService(commentDao, likeDao, likeValidator, eventBus)
  lazy val emailService = new EmailService(config)
  lazy val emailScheduler = new EmailScheduler(actorSystem, EmailScheduler.createActor(actorSystem, emailService))
  lazy val templateEngine = new TemplateEngine()
  lazy val invitationsService = new InvitationService(invitationDao, userDao, emailService, config, DefaultUniqueHashGenerator, templateEngine)
  lazy val notificationService = new NotificationService(emailScheduler, templateEngine, config, toReviewCommitsFinder, followupFinder, clock)


  lazy val welcomeFollowupsGenerator = new WelcomeFollowupsGenerator(internalUserDao, commentDao, likeDao, followupDao, commitInfoDao, templateEngine)
  lazy val followupGeneratorForPriorReactions = new FollowupsGeneratorForReactionsPriorUserRegistration(commentDao, likeDao, followupDao, commitInfoDao, config)

  lazy val authenticator = new UserPasswordAuthenticator(userDao, eventBus)
  lazy val emptyGithubAuthenticator = new GitHubEmptyAuthenticator(userDao)

  lazy val newUserAdder = new NewUserAdder(userDao, eventBus, afterUserRegistered, followupGeneratorForPriorReactions, welcomeFollowupsGenerator)
  lazy val afterUserRegistered = new AfterUserRegistered(repositoryCache, reviewedCommitsCache, config)
  lazy val afterUserLogin = new AfterUserLogin(reviewedCommitsCache)

  lazy val registerService = new RegisterService(userDao, newUserAdder, invitationsService, notificationService)

  lazy val diffWithCommentsService = new DiffWithCommentsService(allCommitsFinder, reactionFinder, new DiffService(diffLoader, repository))

  lazy val statsAggregator = new StatsAggregator(statsFinder, InstanceId, config, repository)


  lazy val loginUserUseCase = new LoginUserUseCase(userDao, afterUserLogin)
  lazy val addCommentUseCase = new AddCommentUseCase(userReactionService, followupService, eventBus, licenceService)
  lazy val reviewCommitUseCase = new ReviewCommitUseCase(commitInfoDao, reviewedCommitsCache, eventBus, licenceService)
  lazy val unlikeUseCaseFactory = new UnlikeUseCase(likeValidator, userReactionService, licenceService)
  lazy val likeUseCase = new LikeUseCase(userReactionService, licenceService)
  lazy val changeUserSettingsUseCase = new ChangeUserSettingsUseCase(userDao, licenceService)
  lazy val followupDoneUseCase = new FollowupDoneUseCase(followupService, licenceService)
  lazy val registerLicenceUseCase = new RegisterLicenceUseCase(licenceService, userDao)
  lazy val registerNewUserUseCase = new RegisterNewUserUseCase(licenceService, registerService, userDao)
  lazy val generateInvitationCodeUseCase = new GenerateInvitationCodeUseCase(invitationsService, userDao)
  lazy val sendInvitationEmailUseCase = new SendInvitationEmailUseCase(invitationsService, userDao)
  lazy val modifyUserDetailsUseCase = new ModifyUserDetailsUseCase(userDao, licenceService)

  lazy val licenceService = new LicenceService(InstanceId, instanceParamsDao, userDao)(clock)

  lazy val instanceParamsService = new InstanceParamsService(instanceParamsDao)
  lazy val InstanceId = instanceParamsService.readOrCreateInstanceId

  lazy val statsHTTPRequestSender = new StatsHTTPRequestSender(config)
  lazy val instanceRunStatsSender = new InstanceRunStatsSender(statsHTTPRequestSender)

  lazy val cacheBackend = new PersistentBackendForCache(commitInfoDao, branchStateDao)
  lazy val repositoriesCache = new RepositoriesCache(cacheBackend, config)
  lazy val repositoryCache = new RepositoryCache(repository, cacheBackend, config)
  lazy val reviewedCommitsCache = new UserReviewedCommitsCache(userDao, reviewedCommitsDao)

  lazy val toReviewCommitsFinder = new ToReviewCommitsFinder(
    repositoryCache,
    userDao,
    new ToReviewBranchCommitsFilter(reviewedCommitsCache, config),
    new ToReviewCommitsViewBuilder(userDao, commitInfoDao)
  )

  lazy val allCommitsFinder = new AllCommitsFinder(
    repositoryCache,
    commitInfoDao,
    userDao,
    new AllCommitsViewBuilder(commitInfoDao, config, userDao, reviewedCommitsCache)
  )
}