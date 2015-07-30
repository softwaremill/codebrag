package com.softwaremill.codebrag.auth

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.softwaremill.codebrag.common.Utils
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra.auth.ScentryStrategy
import org.scalatra.{Cookie, CookieOptions, ScalatraBase}

class RememberMeStrategy(protected val app: ScalatraBase, rememberMe: Boolean, val authenticator: Authenticator) extends ScentryStrategy[User] {

  override def name: String = RememberMe.name

  override def afterAuthenticate(winningStrategy: String, user: User)(implicit request: HttpServletRequest, response: HttpServletResponse) {
    if (winningStrategy == name || (winningStrategy == UserPassword.name && rememberMe)) {
      val usedToken = app.cookies.get(cookieKey).flatMap(to => user.tokens.find(_.token == to))

      val newToken = authenticator.deleteOldSoonAndCreateNewToken(user, usedToken)

      app.response.addHeader("Set-Cookie",
        Cookie(cookieKey, newToken.token)(CookieOptions(path = "/", secure = false, maxAge = Utils.OneWeek, httpOnly = true)).toCookieString)
    }
  }

  override def isValid(implicit request: HttpServletRequest) = {
    app.cookies.get(cookieKey).flatMap(Some(_)).isDefined
  }

  override def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse) = {
    app.cookies.get(cookieKey).flatMap(authenticator.authenticateWithToken)
  }

  override def beforeLogout(user: User)(implicit request: HttpServletRequest, response: HttpServletResponse) {
    app.response.addHeader("Set-Cookie",
      Cookie(cookieKey, "")(CookieOptions(path = "/", secure = false, maxAge = 0, httpOnly = true)).toCookieString)
  }

  protected def cookieKey(implicit request: HttpServletRequest) = {
    AuthUtils.cookieKey(name)(request)
  }

}

object RememberMe {

  val name = "RememberMe"

}