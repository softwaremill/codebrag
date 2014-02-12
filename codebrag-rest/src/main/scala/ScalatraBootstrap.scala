import com.softwaremill.codebrag.dao.mongo.MongoInit
import com.softwaremill.codebrag.dao.user.InternalUserDAO
import com.softwaremill.codebrag.domain.InternalUser
import com.softwaremill.codebrag.rest._
import com.softwaremill.codebrag.service.notification.UserNotificationSenderActor
import com.softwaremill.codebrag.service.updater.RepositoryUpdateScheduler
import com.softwaremill.codebrag.stats.StatsSendingScheduler
import com.softwaremill.codebrag._
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

    val _config = new Config {
      def rootConfig = ConfigFactory.load()
    }

    val beans = new Beans with EventingConfiguration with MongoDaos {
      val config = _config
    }

    import beans._

    MongoInit.initialize(config)
    ensureInternalCodebragUserExists(beans.internalUserDao)

    if(config.userNotifications) {
      UserNotificationSenderActor.initialize(actorSystem, heartbeatStore, notificationCountFinder, userDao, clock, notificationService, config)
    }

    if(config.sendStats) {
      instanceRunStatsSender.sendInstanceRunInfoImmediately(instanceSettings)
      StatsSendingScheduler.initialize(actorSystem, statsAggregator, statsHTTPRequestSender, config)
    } else {
      logger.info("Sending anonymous statistics was disabled - not scheduling stats calculation")
    }

    val repositoryUpdateActor = RepositoryUpdateScheduler.initialize(actorSystem, repoDataProducer, commitImportService)
    context.mount(new UsersServlet(authenticator, registerService, userDao, config, swagger), Prefix + "users")
    context.mount(new UsersSettingsServlet(authenticator, userDao, changeUserSettingsUseCase), Prefix + "users/settings")
    context.mount(new CommitsServlet(authenticator, reviewableCommitsFinder, allCommitsFinder, reactionFinder, commentActivity,
      commitReviewActivity, userReactionService, userDao, swagger, diffWithCommentsService, unlikeUseCaseFactory), Prefix + CommitsServlet.MAPPING_PATH)
    context.mount(new FollowupsServlet(authenticator, swagger, followupFinder, followupService), Prefix + FollowupsServlet.MappingPath)
    context.mount(new SwaggerApiDoc(swagger), "/api-docs/*")
    context.mount(new RefreshRepoDataServlet(repoDataProducer, repositoryUpdateActor), RefreshRepoDataServlet.MappingPath)
    context.mount(new VersionServlet, Prefix + "version")
    context.mount(new ConfigServlet(config, authenticator), Prefix + "config")
    context.mount(new InvitationServlet(authenticator, invitationsService), Prefix + "invitation")
    context.mount(new RepositorySyncServlet(actorSystem, repositoryUpdateActor), RepositorySyncServlet.Mapping)
    context.mount(new UpdatesServlet(authenticator, notificationCountFinder, heartbeatStore, clock), Prefix + UpdatesServlet.Mapping)
    context.mount(new RepoStatusServlet(authenticator, repoDataProducer, repoStatusDao), Prefix + RepoStatusServlet.Mapping)

    if (config.demo) {
      context.mount(new GithubAuthorizationServlet(emptyGithubAuthenticator, ghService, userDao, newUserAdder, config), Prefix + "github")
    }

    InstanceContext.put(context, beans)
  }

  private def ensureInternalCodebragUserExists(internalUserDao: InternalUserDAO) {
    internalUserDao.createIfNotExists(InternalUser(InternalUser.WelcomeFollowupsAuthorName))
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)

    val actorSystem = InstanceContext.get(context).actorSystem
    actorSystem.shutdown()
    actorSystem.awaitTermination()
  }

}
