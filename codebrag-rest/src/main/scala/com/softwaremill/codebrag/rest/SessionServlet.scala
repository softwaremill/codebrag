package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.{AfterUserLogin, Authenticator}

class SessionServlet(val authenticator: Authenticator, afterLogin: AfterUserLogin) extends JsonServletWithAuthentication {

  post("/") {
    authenticate() match {
      case Some(loggedUser) => {
        afterLogin.postLogin(loggedUser)
        loggedUser
      }
      case _ => halt(401, "Invalid login and/or password")
    }
  }

  get("/") {
    haltIfNotAuthenticated()
    user
  }

  delete("/") {
    if (isAuthenticated) {
      // call logout only when logged in to avoid NPE
      logOut()
    }
  }

  // update user details: password, admin, active
  put("/:userId") {

  }

  override def login: String = (parsedBody \ "login").extractOpt[String].getOrElse("")
  override def password: String = (parsedBody \ "password").extractOpt[String].getOrElse("")
  override def rememberMe: Boolean = (parsedBody \ "rememberme").extractOpt[Boolean].getOrElse(false)

}

object SessionServlet {
  val MappingPath = "session"
}
