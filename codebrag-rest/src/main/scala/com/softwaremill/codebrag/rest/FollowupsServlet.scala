package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra.swagger.{SwaggerSupport, Swagger}
import org.scalatra.json.JacksonJsonSupport
import com.softwaremill.codebrag.service.followups.FollowupService
import com.softwaremill.codebrag.dao.reporting._
import java.util.Date
import com.softwaremill.codebrag.dao.reporting.FollowupsList
import scala.Some
import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.domain.User
import org.bson.types.ObjectId

class FollowupsServlet(val authenticator: Authenticator,
                       val swagger: Swagger,
                       followupsService: FollowupService,
                       followupFinder: FollowupFinder,
                       userDAO: UserDAO)
  extends JsonServletWithAuthentication with FollowupsServletSwaggerDefinition with JacksonJsonSupport {

  get("/", operation(getOperation)) {
    haltIfNotAuthenticated
    val login = user.login
    val userOption = userDAO.findByLoginOrEmail(login)
    val userId = userOption.getOrElse(throw new IllegalStateException(s"User logged in as $login not found!")).id
    followupFinder.findAllFollowupsForUser(userId)
  }
}

trait FollowupsServletSwaggerDefinition extends SwaggerSupport {

  override protected val applicationName = Some(FollowupsServlet.MappingPath)
  protected val applicationDescription: String = "Follow-ups endpoint"

  val getOperation = apiOperation[FollowupsList]("get")
}

object FollowupsServlet {
  val MappingPath = "followups"
}