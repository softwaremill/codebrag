import com.softwaremill.codebrag.backup.BackupScheduler
import com.softwaremill.codebrag.dao.mongo.MongoInit
import com.softwaremill.codebrag.dao.{SQLDaos, MongoDaos}
import com.softwaremill.codebrag.dao.sql.{SQLEmbeddedDbBackup, SQLDatabase}
import com.softwaremill.codebrag.dao.user.InternalUserDAO
import com.softwaremill.codebrag.domain.InternalUser
import com.softwaremill.codebrag.repository.config.RepoDataDiscovery
import com.softwaremill.codebrag.repository.Repository
import com.softwaremill.codebrag.rest._
import com.softwaremill.codebrag.service.config.RepositoryConfig
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

    val repoData = discoverRepositoryOrExit(_config)
    val _repository = Repository.buildUsing(repoData)

    val beans = initializeBeans(_config, _repository)
    import beans._

    repositoryStateCache.initialize()
    reviewedCommitsCache.initialize()

    setupEvents()
    ensureInternalCodebragUserExists(beans.internalUserDao)

    if(config.userNotifications) {
      UserNotificationSenderActor.initialize(actorSystem, heartbeatDao, toReviewCommitsFinder, followupFinder, userDao, clock, notificationService, config)
    }

    if(config.sendStats) {
      instanceRunStatsSender.sendInstanceRunInfoImmediately(InstanceId, config.appVersion)
      StatsSendingScheduler.initialize(actorSystem, statsAggregator, statsHTTPRequestSender, config)
    } else {
      logger.info("Sending anonymous statistics was disabled - not scheduling stats calculation")
    }

    val repoUpdateActor = RepositoryUpdateScheduler.initialize(actorSystem, _repository, commitImportService)
    context.mount(new UsersServlet(authenticator, registerService, registerNewUserUseCase, afterUserLogin, userDao, config, swagger), Prefix + "users")
    context.mount(new UsersSettingsServlet(authenticator, userDao, changeUserSettingsUseCase), Prefix + "users/settings")
    context.mount(new CommitsServlet(authenticator, toReviewCommitsFinder, allCommitsFinder, reactionFinder, addCommentUseCase,
      reviewCommitUseCase, userReactionService, userDao, swagger, diffWithCommentsService, unlikeUseCaseFactory, likeUseCase), Prefix + CommitsServlet.MAPPING_PATH)
    context.mount(new FollowupsServlet(authenticator, swagger, followupFinder, followupDoneUseCase), Prefix + FollowupsServlet.MappingPath)
    context.mount(new SwaggerApiDoc(swagger), "/api-docs/*")
    context.mount(new VersionServlet(config), Prefix + "version")
    context.mount(new ConfigServlet(config, authenticator), Prefix + "config")
    context.mount(new InvitationServlet(authenticator, invitationsService), Prefix + "invitation")
    context.mount(new RepositorySyncServlet(actorSystem, repoUpdateActor), RepositorySyncServlet.Mapping)
    context.mount(new UpdatesServlet(authenticator, followupFinder, heartbeatDao, toReviewCommitsFinder, clock), Prefix + UpdatesServlet.Mapping)
    context.mount(new RepoStatusServlet(authenticator, _repository, repoStatusDao), Prefix + RepoStatusServlet.Mapping)
    context.mount(new AvailableBranchesServlet(authenticator, repositoryStateCache), Prefix + AvailableBranchesServlet.MountPath)
    context.mount(new LicenceServlet(licenceService, registerLicenceUseCase, authenticator), Prefix + LicenceServlet.MountPath)

    if (config.demo) {
      context.mount(new GithubAuthorizationServlet(emptyGithubAuthenticator, ghService, userDao, newUserAdder, config), Prefix + "github")
    }

    context.mount(new TimingFilter, "/*")

    InstanceContext.put(context, beans)
  }


  def initializeBeans(_config: AllConfig, _repository: Repository): Beans with EventingConfiguration = {
    if (_config.isEmbeddedStorage) {
      val beans = new Beans with EventingConfiguration with SQLDaos {
        val config = _config
        val sqlDatabase = SQLDatabase.createEmbedded(config)
        val repository = _repository
      }
      beans.sqlDatabase.updateSchema()
      BackupScheduler.initialize(beans.actorSystem, new SQLEmbeddedDbBackup(beans.sqlDatabase, beans.config, beans.clock), beans.config, beans.clock)
      beans
    } else {
      MongoInit.initialize(_config)
      new Beans with EventingConfiguration with MongoDaos {
        val config = _config
        val repository = _repository
      }
    }
  }

  private def ensureInternalCodebragUserExists(internalUserDao: InternalUserDAO) {
    internalUserDao.createIfNotExists(InternalUser(InternalUser.WelcomeFollowupsAuthorName))
  }

  private def discoverRepositoryOrExit(repoConfig: RepositoryConfig) = {
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

    if (beans.config.isEmbeddedStorage) {
      // I know, ugly, I'm ashamed
      beans.asInstanceOf[SQLDaos].sqlDatabase.close()
    }

    val actorSystem = beans.actorSystem
    actorSystem.shutdown()
    actorSystem.awaitTermination()
  }

}
