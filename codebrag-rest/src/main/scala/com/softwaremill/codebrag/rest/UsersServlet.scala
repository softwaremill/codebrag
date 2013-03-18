package com.softwaremill.codebrag.rest

import org.scalatra._
import com.softwaremill.codebrag.service.user.UserService
import com.softwaremill.codebrag.service.data.UserJson
import swagger.{Swagger, SwaggerSupport}

class UsersServlet(val userService: UserService, val swagger: Swagger) extends JsonServletWithAuthentication with UsersServletSwaggerDefinition with CookieSupport {

  post(operation(loginOperation)) {
    val userOpt: Option[UserJson] = authenticate()
    userOpt match {
      case Some(loggedUser) =>
        loggedUser
      case _ =>
        halt(401, "Invalid login and/or password")
    }
  }

  get(operation(userProfileOperation)) {
    haltIfNotAuthenticated()
    user
  }

  get("/logout", operation(logoutOperation)) {
    if (isAuthenticated) {
      // call logout only when logged in to avoid NPE
      logOut()
    }
  }

  override def login: String = {
    (parsedBody \ "login").extractOpt[String].getOrElse("")
  }

  override def password: String = {
    (parsedBody \ "password").extractOpt[String].getOrElse("")
  }

  override def rememberMe: Boolean = {
    (parsedBody \ "rememberme").extractOpt[Boolean].getOrElse(false)
  }

  def email: String = {
    (parsedBody \ "email").extractOpt[String].getOrElse("")
  }

}

object UsersServlet {
  val MAPPING_PATH = "users"
}


trait UsersServletSwaggerDefinition extends SwaggerSupport {

  override protected val applicationName = Some(UsersServlet.MAPPING_PATH)
  protected val applicationDescription: String = "User and session management endpoint"

  val loginOperation = apiOperation[UserJson]("login")
    .summary("log user in")
    .parameter(bodyParam[String]("login").description("user login").required)
    .parameter(bodyParam[String]("password").description("user password").required)
    .parameter(bodyParam[Boolean]("rememberme").description("whether user session should be remembered").required)

  val userProfileOperation = apiOperation[UserJson]("userProfile")
    .summary("gets logged in user")
    .notes("Requires user to be authenticated")

  val logoutOperation = apiOperation[Unit]("logout")
    .summary("logs user out")
    .notes("Requires user to be authenticated")

}
