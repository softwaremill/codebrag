import com.mongodb.Mongo
import com.softwaremill.codebrag.rest.SwaggerApiDoc
import java.util.concurrent.TimeUnit
import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB}
import com.softwaremill.codebrag.Beans
import com.softwaremill.codebrag.rest.{UsersServlet, UptimeServlet}
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
    MongoDB.defineDb(DefaultMongoIdentifier, new Mongo, "codebrag")

    scheduler.scheduleAtFixedRate(emailSendingService, 60, 1, TimeUnit.SECONDS)

    context.mount(new UptimeServlet, Prefix + "uptime")
    context.mount(new UsersServlet(userService, swagger), Prefix + "users")

    context.mount(new SwaggerApiDoc(swagger), Prefix + "api-docs/*")

    context.put("codebrag", this)
  }


  override def destroy(context: ServletContext) {
    scheduler.shutdownNow()
  }

}
