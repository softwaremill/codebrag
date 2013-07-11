package com.softwaremill.codebrag.auth

import org.scalatra._
import org.scalatra.auth.ScentryAuthStore.CookieAuthStore
import org.scalatra.auth.{ Scentry, ScentryConfig, ScentrySupport }
import scala.Some
import com.softwaremill.codebrag.common.{ Utils, JsonWrapper }
import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.service.data.UserJson
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

/**
 * It should be used with each servlet to support RememberMe functionality for whole application
 */
trait RememberMeSupport extends AuthenticationSupport {

  self: ScalatraBase =>

  before() {
    if (!isAuthenticated) {
      scentry.authenticate(RememberMe.name)
    }
  }

}

trait AuthenticationSupport extends ScentrySupport[UserJson] {

  self: ScalatraBase =>

  def authenticator: Authenticator

  override protected def registerAuthStrategies {
    scentry.register(RememberMe.name, app => new RememberMeStrategy(app, rememberMe, authenticator))
    scentry.register(UserPassword.name, app => new UserPasswordStrategy(app, login, password, authenticator))
  }

  protected def fromSession = {
    case id: String => {
      val userOpt: Option[UserJson] = authenticator.findByLogin(id)
      userOpt match {
        case Some(u) => u
        case _ => null
      }
    }
  }

  protected def toSession = {
    case usr: UserJson => usr.login
  }

  override protected def configureScentry {
    val authCookieOptions = cookieOptions.copy(path = "/", secure = false, maxAge = Utils.OneWeek, httpOnly = true)
    scentry.store = new CookieAuthStore(self) {
      override def invalidate()(implicit request: HttpServletRequest, response: HttpServletResponse) {
        cookies.update(Scentry.scentryAuthKey, user.token)(authCookieOptions.copy(maxAge = 0))
      }
    }
    scentry.unauthenticated {
      Unauthorized(JsonWrapper("Unauthenticated"))
    }
  }

  // Define type to avoid casting as (new ScentryConfig {}).asInstanceOf[ScentryConfiguration]
  type ScentryConfiguration = ScentryConfig

  protected def scentryConfig = {
    new ScentryConfig {}
  }

  /**
   * Implement to configure login process, must be only done on Login form
   */
  protected def login: String = ""

  protected def password: String = ""

  protected def rememberMe: Boolean = false

  def haltIfNotAuthenticated() {
    if (isAuthenticated == false) {
      halt(401, "User not logged in")
    }
  }

  def haltWithForbiddenIf(f: Boolean) {
    if (f) halt(403, "Action forbidden")
  }

}