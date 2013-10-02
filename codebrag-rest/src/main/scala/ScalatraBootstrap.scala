import com.softwaremill.codebrag.dao.MongoInit
import com.softwaremill.codebrag.rest._
import com.softwaremill.codebrag.rest.debug.DebugServlet
import com.softwaremill.codebrag.service.notification.NotificationScheduler
import com.softwaremill.codebrag.service.updater.RepositoryUpdateScheduler
import com.softwaremill.codebrag.{EventingConfiguration, Beans}
import java.util.Locale
import org.scalatra._
import javax.servlet.ServletContext

/**
 * This is the ScalatraBootstrap codebrag file. You can use it to mount servlets or
 * filters. It's also a good place to put initialization code which needs to
 * run at application start (e.g. database configurations), and init params.
 */
class ScalatraBootstrap extends LifeCycle with Beans with EventingConfiguration {
  val Prefix = "/rest/"

  override def init(context: ServletContext) {
    Locale.setDefault(Locale.US) // set default locale to prevent Scalatra from sending cookie expiration date in polish format :)

    MongoInit.initialize(config)

    val repositoryUpdateActor = RepositoryUpdateScheduler.initialize(actorSystem, repoDataProducer, commitImportService)
    NotificationScheduler.initialize(actorSystem, heartbeatStore, notificationCountFinder, emailScheduler, userDao, templateEngine, config, clock)
    context.mount(new UsersServlet(authenticator, registerService, swagger), Prefix + "users")
    context.mount(new CommitsServlet(authenticator, reviewableCommitsFinder, allCommitsFinder, reactionFinder, commentActivity, commitReviewTaskDao, userReactionService, userDao, swagger, diffWithCommentsService, unlikeUseCaseFactory), Prefix + CommitsServlet.MAPPING_PATH)
    context.mount(new FollowupsServlet(authenticator, swagger, followupFinder, followupService), Prefix + FollowupsServlet.MappingPath)
    context.mount(new SwaggerApiDoc(swagger), "/api-docs/*")
    context.mount(new DebugServlet(repoDataProducer, commitImportService, config), Prefix + DebugServlet.MappingPath)
    context.mount(new RefreshRepoDataServlet(repoDataProducer, repositoryUpdateActor), RefreshRepoDataServlet.MappingPath)
    context.mount(new VersionServlet, Prefix + "version")
    context.mount(new ConfigServlet(config), Prefix + "config")
    context.mount(new InvitationServlet(authenticator, invitationsService), Prefix + "invitation")
    context.mount(new RepositorySyncServlet(actorSystem, repositoryUpdateActor), RepositorySyncServlet.Mapping)
    context.mount(new UpdatesServlet(authenticator, notificationCountFinder, heartbeatStore, clock), Prefix + UpdatesServlet.Mapping)

    if(config.demo) {
      context.mount(new GithubAuthorizationServlet(emptyGithubAuthenticator, ghService, userDao, newUserAdder, config), Prefix + "github")
    }

    context.put("codebrag", this)
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    actorSystem.shutdown()
    actorSystem.awaitTermination()
  }
}