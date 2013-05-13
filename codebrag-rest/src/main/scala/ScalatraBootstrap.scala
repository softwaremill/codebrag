import com.softwaremill.codebrag.dao.MongoInit
import com.softwaremill.codebrag.rest._
import com.softwaremill.codebrag.Beans
import org.scalatra._
import javax.servlet.ServletContext

/**
 * This is the ScalatraBootstrap codebrag file. You can use it to mount servlets or
 * filters. It's also a good place to put initialization code which needs to
 * run at application start (e.g. database configurations), and init params.
 */
class ScalatraBootstrap extends LifeCycle with Beans {
  val Prefix = "/rest/"

  override def init(context: ServletContext) {
    MongoInit.initialize()
    context.mount(new UptimeServlet, Prefix + "uptime")
    context.mount(new UsersServlet(authenticator, swagger), Prefix + "users")
    context.mount(new CommitsServlet(authenticator, commitListFinder, commentListFinder, commentActivity, commitReviewTaskDao, userDao, swagger, diffWithCommentsService, importerFactory), Prefix + CommitsServlet.MAPPING_PATH)
    context.mount(new GithubAuthorizationServlet(authenticator, ghService, userDao), Prefix + "github")
    context.mount(new FollowupsServlet(authenticator, swagger, followupFinder, followupService), Prefix + FollowupsServlet.MappingPath)
    context.mount(new NotificationCountServlet(authenticator, swagger), Prefix + NotificationCountServlet.MappingPath)
    context.mount(new SwaggerApiDoc(swagger), Prefix + "api-docs/*")

    context.put("codebrag", this)
  }


}
