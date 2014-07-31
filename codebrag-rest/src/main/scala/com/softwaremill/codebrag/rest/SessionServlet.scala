package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra
import com.softwaremill.codebrag.finders.user.UserFinder
import com.softwaremill.codebrag.usecases.user.{LoginForm, LoginFailedException, LoginUserUseCase}

class SessionServlet(val authenticator: Authenticator, loginUseCase: LoginUserUseCase, userFinder: UserFinder) extends JsonServletWithAuthentication {

  post("/") {
    try {
      loginUseCase.execute(LoginForm(login, password, rememberMe)) {
        authenticate()
      } match {
        case Right(user) => scalatra.Ok(user)
        case Left(errors) => scalatra.Unauthorized(errors)
      }
    } catch {
      case e: LoginFailedException => scalatra.Unauthorized(Map("errors" -> List(e.msg)))
    }
  }

  get("/") {
    haltIfNotAuthenticated()
    userFinder.findLoggedInUser(user)
  }

  delete("/") {
    if (isAuthenticated) {
      // call logout only when logged in to avoid NPE
      logOut()
    }
  }

  override def login: String = (parsedBody \ "login").extractOpt[String].getOrElse("")
  override def password: String = (parsedBody \ "password").extractOpt[String].getOrElse("")
  override def rememberMe: Boolean = (parsedBody \ "rememberme").extractOpt[Boolean].getOrElse(false)

}

object SessionServlet {
  val MappingPath = "session"
}
