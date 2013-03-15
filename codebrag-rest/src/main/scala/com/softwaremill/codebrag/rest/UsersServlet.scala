package com.softwaremill.codebrag.rest

import org.scalatra._
import com.softwaremill.codebrag.common.JsonWrapper
import com.softwaremill.codebrag.service.user.UserService
import com.softwaremill.codebrag.service.data.UserJson
import org.apache.commons.lang3.StringEscapeUtils._

class UsersServlet(val userService: UserService) extends JsonServletWithAuthentication with CookieSupport {

  post() {
    val userOpt: Option[UserJson] = authenticate()
    userOpt match {
      case Some(loggedUser) =>
        loggedUser
      case _ =>
        halt(401, "Invalid login and/or password")
    }
  }

  get() {
    haltIfNotAuthenticated()
    user
  }

  get("/logout") {
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

