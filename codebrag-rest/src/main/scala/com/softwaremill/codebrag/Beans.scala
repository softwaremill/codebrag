package com.softwaremill.codebrag

import com.softwaremill.codebrag.cache.{PersistentBackendForCache, RepositoriesCache, UserReviewedCommitsCache}
import com.softwaremill.codebrag.common.{IdGenerator, ObjectIdGenerator, RealTimeClock}
import com.softwaremill.codebrag.dao.Daos
import com.softwaremill.codebrag.finders.browsingcontext.UserBrowsingContextFinder
import com.softwaremill.codebrag.finders.commits.all.{AllCommitsFinder, AllCommitsViewBuilder}
import com.softwaremill.codebrag.finders.commits.toreview.{ToReviewBranchCommitsFilter, ToReviewCommitsFinder, ToReviewCommitsViewBuilder}
import com.softwaremill.codebrag.finders.user.UserFinder
import com.softwaremill.codebrag.instance.InstanceParamsService
import com.softwaremill.codebrag.repository.Repository
import com.softwaremill.codebrag.service.actors.ActorSystemSupport
import com.softwaremill.codebrag.service.comments.{LikeValidator, UserReactionService}
import com.softwaremill.codebrag.service.commits._
import com.softwaremill.codebrag.service.commits.jgit.JgitDiffLoader
import com.softwaremill.codebrag.service.diff.{DiffService, DiffWithCommentsService}
import com.softwaremill.codebrag.service.email.{EmailScheduler, EmailService}
import com.softwaremill.codebrag.service.events.akka.AkkaEventBus
import com.softwaremill.codebrag.service.followups.{FollowupService, FollowupsGeneratorForReactionsPriorUserRegistration, WelcomeFollowupsGenerator}
import com.softwaremill.codebrag.service.invitations.{DefaultUniqueHashGenerator, InvitationService}
import com.softwaremill.codebrag.service.notification.NotificationService
import com.softwaremill.codebrag.service.templates.TemplateEngine
import com.softwaremill.codebrag.service.user._
import com.softwaremill.codebrag.stats.{InstanceRunStatsSender, StatsAggregator, StatsHTTPRequestSender}
import com.softwaremill.codebrag.usecases._
import com.softwaremill.codebrag.usecases.branches.{ListRepositoryBranches, StartWatchingBranch, StopWatchingBranch}
import com.softwaremill.codebrag.usecases.emailaliases.{AddUserAliasUseCase, DeleteUserAliasUseCase}
import com.softwaremill.codebrag.usecases.notifications.FindUserNotifications
import com.softwaremill.codebrag.usecases.reactions._
import com.softwaremill.codebrag.usecases.registration.{ListRepoBranchesAfterRegistration, ListRepositoriesAfterRegistration, UnwatchBranchAfterRegistration, WatchBranchAfterRegistration}
import com.softwaremill.codebrag.usecases.user._
import com.softwaremill.codebrag.usecases.team.{AddTeamUseCase, DeleteTeamUseCase, AddTeamMemberUseCase, DeleteTeamMemberUseCase, ModifyTeamMemberUseCase}
import com.softwaremill.codebrag.finders.team.TeamFinder

trait Beans extends ActorSystemSupport with Daos {

  def config: AllConfig
  def repository: Repository

  implicit lazy val clock = RealTimeClock
  implicit lazy val idGenerator: IdGenerator = new ObjectIdGenerator
  lazy val self = this
  lazy val eventBus = new AkkaEventBus(actorSystem)
  lazy val followupService = new FollowupService(followupDao, commitInfoDao, commentDao, userDao)
  lazy val likeValidator = new LikeValidator(commitInfoDao, likeDao, userDao)
  lazy val userReactionService = new UserReactionService(commentDao, likeDao, likeValidator, eventBus)
  lazy val emailService = new EmailService(config)
  lazy val emailScheduler = new EmailScheduler(actorSystem, EmailScheduler.createActor(actorSystem, emailService))
  lazy val templateEngine = new TemplateEngine()
  lazy val invitationsService = new InvitationService(invitationDao, userDao, emailService, config, DefaultUniqueHashGenerator, templateEngine)
  lazy val notificationService = new NotificationService(emailScheduler, templateEngine, config, toReviewCommitsFinder, clock)


  lazy val welcomeFollowupsGenerator = new WelcomeFollowupsGenerator(internalUserDao, commentDao, likeDao, followupDao, commitInfoDao, templateEngine)
  lazy val followupGeneratorForPriorReactions = new FollowupsGeneratorForReactionsPriorUserRegistration(commentDao, likeDao, followupDao, commitInfoDao, config)

  lazy val authenticator = new UserPasswordAuthenticator(userDao, eventBus, actorSystem.dispatcher)

  lazy val afterUserRegistered = new AfterUserRegistered(repositoriesCache, reviewedCommitsCache,userRepoDetailsDao, config)

  lazy val registerService = new RegisterService(userDao, eventBus, afterUserRegistered, notificationService, followupGeneratorForPriorReactions, welcomeFollowupsGenerator)

  lazy val diffWithCommentsService = new DiffWithCommentsService(allCommitsFinder, reactionFinder, new DiffService(diffLoader, repositoriesCache))

  lazy val statsAggregator = new StatsAggregator(statsFinder, InstanceId, config, repository)


  lazy val loginUserUseCase = new LoginUserUseCase(userDao, userFinder)
  lazy val addCommentUseCase = new AddCommentUseCase(userReactionService, followupService, eventBus)
  lazy val reviewCommitUseCase = new ReviewCommitUseCase(commitInfoDao, reviewedCommitsCache, eventBus)

  lazy val reviewAllCommitsUseCase = new ReviewAllCommitsUseCase(
    userDao,
    eventBus,
    commitInfoDao,
    repositoriesCache,
    new ToReviewBranchCommitsFilter(reviewedCommitsCache, config),
    reviewedCommitsCache
  )

  lazy val unlikeUseCaseFactory = new UnlikeUseCase(likeValidator, userReactionService)
  lazy val likeUseCase = new LikeUseCase(userReactionService)
  lazy val changeUserSettingsUseCase = new ChangeUserSettingsUseCase(userDao)
  lazy val followupDoneUseCase = new FollowupDoneUseCase(followupService)
  lazy val registerNewUserUseCase = new RegisterNewUserUseCase(registerService, new UserRegistrationValidator(invitationsService, userDao))

  lazy val listReposAfterRegistration = new ListRepositoriesAfterRegistration(repositoriesCache, invitationsService)
  lazy val listRepoBranchesAfterRegistration = new ListRepoBranchesAfterRegistration(listRepoBranches, invitationsService)
  lazy val watchBranchAfterRegistration = new WatchBranchAfterRegistration(addBranchToObserved, invitationsService)
  lazy val unwatchBranchAfterRegistration = new UnwatchBranchAfterRegistration(removeBranchFromObserved, invitationsService)

  lazy val generateInvitationCodeUseCase = new GenerateInvitationCodeUseCase(invitationsService, userDao)
  lazy val sendInvitationEmailUseCase = new SendInvitationEmailUseCase(invitationsService, userDao)
  lazy val modifyUserDetailsUseCase = new ModifyUserDetailsUseCase(userDao)
  lazy val updateUserBrowsingContextUseCase = new UpdateUserBrowsingContextUseCase(userRepoDetailsDao)
  lazy val addUserAliasUseCase = new AddUserAliasUseCase(userAliasDao, userDao)
  lazy val deleteUserAliasUseCase = new DeleteUserAliasUseCase(userAliasDao)
  lazy val addBranchToObserved = new StartWatchingBranch(userObservedBranchesDao)
  lazy val removeBranchFromObserved = new StopWatchingBranch(userObservedBranchesDao)
  lazy val listRepoBranches = new ListRepositoryBranches(repositoriesCache, userObservedBranchesDao)
  lazy val findUserNotifications = new FindUserNotifications(followupFinder, heartbeatDao, toReviewCommitsFinder, userObservedBranchesDao)

  lazy val instanceParamsService = new InstanceParamsService(instanceParamsDao)
  lazy val InstanceId = instanceParamsService.readOrCreateInstanceId

  lazy val statsHTTPRequestSender = new StatsHTTPRequestSender(config)
  lazy val instanceRunStatsSender = new InstanceRunStatsSender(statsHTTPRequestSender)

  lazy val cacheBackend = new PersistentBackendForCache(commitInfoDao, branchStateDao)
  lazy val repositoriesCache = new RepositoriesCache(cacheBackend, config)
  lazy val reviewedCommitsCache = new UserReviewedCommitsCache(userDao, reviewedCommitsDao, userRepoDetailsDao)

  lazy val toReviewCommitsFinder = new ToReviewCommitsFinder(
    repositoriesCache,
    userDao,
    teamDao,
    userBrowsingContextFinder,
    new ToReviewBranchCommitsFilter(reviewedCommitsCache, config),
    new ToReviewCommitsViewBuilder(userDao, commitInfoDao)
  )

  lazy val allCommitsFinder = new AllCommitsFinder(
    repositoriesCache,
    commitInfoDao,
    userDao,
    new AllCommitsViewBuilder(commitInfoDao, config, userDao, reviewedCommitsCache)
  )

  lazy val userBrowsingContextFinder = new UserBrowsingContextFinder(userRepoDetailsDao, repositoriesCache)
  lazy val userFinder = new UserFinder(userDao, userBrowsingContextFinder)

  lazy val commitImportService = new CommitImportService(repoStatusDao, branchStateDao, repositoriesCache, config, eventBus)
  lazy val diffLoader = new JgitDiffLoader()

  lazy val teamFinder = new TeamFinder(userDao, teamDao)
  lazy val addTeamUseCase = new AddTeamUseCase(teamDao)
  lazy val deleteTeamUseCase = new DeleteTeamUseCase(teamDao)
  lazy val addTeamMemberUseCase = new AddTeamMemberUseCase(teamDao)
  lazy val deleteTeamMemberUseCase = new DeleteTeamMemberUseCase(teamDao)
  lazy val modifyTeamMemberUseCase = new ModifyTeamMemberUseCase(teamDao)
}
