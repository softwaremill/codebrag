import com.softwaremill.codebrag.dao.{MongoConfig, MongoInit}
import com.softwaremill.codebrag.rest._
import com.softwaremill.codebrag.service.config.RepositoryConfig
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

    val config = new MongoConfig with RepositoryConfig {}

    MongoInit.initialize(config)

    RepositoryUpdateScheduler.initialize(actorSystem, importerFactory, config)
    context.mount(new UptimeServlet, Prefix + "uptime")
    context.mount(new UsersServlet(authenticator, swagger), Prefix + "users")
    context.mount(new CommitsServlet(authenticator, commitListFinder, commentListFinder, commentActivity, commitReviewTaskDao, userDao, swagger, diffWithCommentsService, importerFactory), Prefix + CommitsServlet.MAPPING_PATH)
    context.mount(new GithubAuthorizationServlet(authenticator, ghService, userDao, eventBus, reviewTaskGenerator), Prefix + "github")
    context.mount(new FollowupsServlet(authenticator, swagger, followupFinder, followupService), Prefix + FollowupsServlet.MappingPath)
    context.mount(new NotificationCountServlet(authenticator, swagger, notificationCountFinder), Prefix + NotificationCountServlet.MappingPath)
    context.mount(new SwaggerApiDoc(swagger), Prefix + "api-docs/*")

    context.put("codebrag", this)
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    actorSystem.shutdown()
  }
}
