import com.softwaremill.codebrag.backup.BackupScheduler
import com.softwaremill.codebrag.dao.Daos
import com.softwaremill.codebrag.dao.sql.{SQLEmbeddedDbBackup, SQLDatabase}
import com.softwaremill.codebrag.dao.user.InternalUserDAO
import com.softwaremill.codebrag.domain.{UserRepoDetails, InternalUser}
import com.softwaremill.codebrag.repository.config.RepoDataDiscovery
import com.softwaremill.codebrag.repository.Repository
import com.softwaremill.codebrag.rest._
import com.softwaremill.codebrag.service.config.MultiRepoConfig
import com.softwaremill.codebrag.service.notification.UserNotificationSenderActor
import com.softwaremill.codebrag.service.updater.RepositoryUpdateScheduler
import com.softwaremill.codebrag.stats.StatsSendingScheduler
import com.softwaremill.codebrag._
import com.softwaremill.codebrag.web.TimingFilter
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.Logging
import java.util.Locale
import org.scalatra._
import javax.servlet.ServletContext

/**
 * This is the ScalatraBootstrap codebrag file. You can use it to mount servlets or
 * filters. It's also a good place to put initialization code which needs to
 * run at application start (e.g. database configurations), and init params.
 */
class ScalatraBootstrap extends LifeCycle with Logging {
  val Prefix = "/rest/"

  override def init(context: ServletContext) {
    Locale.setDefault(Locale.US) // set default locale to prevent Scalatra from sending cookie expiration date in polish format :)

    val _config = new AllConfig {
      def rootConfig = ConfigFactory.load()
    }

    val repositories = discoverRepositoryOrExit(_config).map(Repository.buildUsing)

    val beans = initializeBeans(_config, repositories.head)
    import beans._

    repositoriesCache.initialize(repositories)

    val repoNames = repositoriesCache.repoNames
    userDao.findAll().foreach { user =>
      logger.debug(s"Synchronizing repositories for ${user.name}")
      val userRepoDetails = userRepoDetailsDao.findAll(user.id)
      val newReposForUser = repoNames.filterNot(rn => userRepoDetails.exists(_.repoName == rn))
      logger.debug(s"Found new repositories for user ${user.name}: $newReposForUser")
      newReposForUser.map({newRepoName =>
        UserRepoDetails(user.id, newRepoName, repositoriesCache.getCheckedOutBranchShortName(newRepoName), clock.nowUtc)
      }).foreach(userRepoDetailsDao.save)
    }


    setupEvents()
    ensureInternalCodebragUserExists(beans.internalUserDao)

    if(config.userNotifications) {
      UserNotificationSenderActor.initialize(actorSystem, heartbeatDao, findUserNotifications, toReviewCommitsFinder, followupFinder, userDao, clock, notificationService, config)
    }

    if(config.sendStats) {
      instanceRunStatsSender.sendInstanceRunInfoImmediately(InstanceId, config.appVersion)
      StatsSendingScheduler.initialize(actorSystem, statsAggregator, statsHTTPRequestSender, config)
    } else {
      logger.info("Sending anonymous statistics was disabled - not scheduling stats calculation")
    }

    RepositoryUpdateScheduler.scheduleUpdates(actorSystem, repositories, commitImportService)
    context.mount(new RegistrationServlet(registerService, registerNewUserUseCase, listReposAfterRegistration, listRepoBranchesAfterRegistration, watchBranchAfterRegistration, unwatchBranchAfterRegistration), Prefix + RegistrationServlet.MappingPath)
    context.mount(new SessionServlet(authenticator, loginUserUseCase, userFinder), Prefix + SessionServlet.MappingPath)
    context.mount(new UsersServlet(authenticator, userFinder, modifyUserDetailsUseCase, config), Prefix + UsersServlet.MappingPath)
    context.mount(new UserAliasesEndpoint(authenticator, addUserAliasUseCase, deleteUserAliasUseCase), Prefix + UserAliasesEndpoint.MappingPath)
    context.mount(new UsersSettingsServlet(authenticator, userDao, changeUserSettingsUseCase), Prefix + "users/settings")
    context.mount(new CommitsServlet(authenticator, toReviewCommitsFinder, allCommitsFinder, reactionFinder, addCommentUseCase,
      reviewCommitUseCase, userReactionService, userDao, diffWithCommentsService, unlikeUseCaseFactory, likeUseCase), Prefix + CommitsServlet.MAPPING_PATH)
    context.mount(new FollowupsServlet(authenticator, followupFinder, followupDoneUseCase), Prefix + FollowupsServlet.MappingPath)
    context.mount(new VersionServlet(config), Prefix + "version")
    context.mount(new ConfigServlet(config, authenticator), Prefix + "config")
    context.mount(new InvitationServlet(authenticator, generateInvitationCodeUseCase, sendInvitationEmailUseCase), Prefix + "invitation")
    context.mount(new UserNotificationsServlet(authenticator, findUserNotifications), Prefix + UserNotificationsServlet.Mapping)
    context.mount(new RepoStatusServlet(authenticator, repositories.head, repoStatusDao), Prefix + RepoStatusServlet.Mapping)
    context.mount(new RepositoryBranchesServlet(authenticator, toReviewCommitsFinder, listRepoBranches, addBranchToObserved, removeBranchFromObserved), Prefix + RepositoryBranchesServlet.MountPath)
    context.mount(new LicenceServlet(licenceService, registerLicenceUseCase, authenticator), Prefix + LicenceServlet.MountPath)
    context.mount(new BrowsingContextServlet(authenticator, userBrowsingContextFinder, updateUserBrowsingContextUseCase), Prefix + BrowsingContextServlet.MappingPath)

    context.mount(new TimingFilter, "/*")

    InstanceContext.put(context, beans)
  }


  def initializeBeans(_config: AllConfig, _repository: Repository): Beans with EventingConfiguration = {
    val beans = new Beans with EventingConfiguration with Daos {
      val config = _config
      val sqlDatabase = SQLDatabase.createEmbedded(config)
      val repository = _repository
    }
    beans.sqlDatabase.updateSchema()
    BackupScheduler.initialize(beans.actorSystem, new SQLEmbeddedDbBackup(beans.sqlDatabase, beans.config, beans.clock), beans.config, beans.clock)
    beans
  }

  private def ensureInternalCodebragUserExists(internalUserDao: InternalUserDAO) {
    internalUserDao.createIfNotExists(InternalUser(InternalUser.WelcomeFollowupsAuthorName))
  }

  private def discoverRepositoryOrExit(repoConfig: MultiRepoConfig) = {
    try {
      RepoDataDiscovery.discoverRepoDataFromConfig(repoConfig)
    } catch {
      case e: Exception => {
        logger.error(e.getMessage)
        logger.error("Codebrag cannot continue, exiting")
        sys.exit(1)
      }
    }
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)

    val beans = InstanceContext.get(context)
    beans.sqlDatabase.close()

    val actorSystem = beans.actorSystem
    actorSystem.shutdown()
    actorSystem.awaitTermination()
  }

}
