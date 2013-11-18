package com.softwaremill.codebrag.auth

import org.scalatra.{ CookieOptions, Cookie, ScalatraBase }
import org.scalatra.auth.ScentryStrategy
import com.softwaremill.codebrag.common.Utils
import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.service.data.UserJson
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

class RememberMeStrategy(protected val app: ScalatraBase, rememberMe: Boolean, val authenticator: Authenticator) extends ScentryStrategy[UserJson] {

  override def name: String = RememberMe.name

  override def afterAuthenticate(winningStrategy: String, user: UserJson)(implicit request: HttpServletRequest, response: HttpServletResponse) {
    if (winningStrategy == name || (winningStrategy == UserPassword.name && rememberMe)) {
      val token = user.token
      app.response.addHeader("Set-Cookie",
        Cookie(cookieKey, token)(CookieOptions(path = "/", secure = false, maxAge = Utils.OneWeek, httpOnly = true)).toCookieString)
    }
  }

  override def isValid(implicit request: HttpServletRequest) = {
    app.cookies.get(cookieKey).flatMap(Some(_)).isDefined
  }

  override def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse) = {
    app.cookies.get(cookieKey).flatMap(authenticator.authenticateWithToken)
  }

  override def beforeLogout(user: UserJson)(implicit request: HttpServletRequest, response: HttpServletResponse) {
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