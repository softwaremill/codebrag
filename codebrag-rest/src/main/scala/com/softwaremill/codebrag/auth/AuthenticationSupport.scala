package com.softwaremill.codebrag.auth

import com.softwaremill.codebrag.common.JsonWrapper
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.service.user.Authenticator
import org.bson.types.ObjectId
import org.scalatra._
import org.scalatra.auth.{ScentryConfig, ScentrySupport}

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

trait AuthenticationSupport extends ScentrySupport[User] {

  self: ScalatraBase =>

  def authenticator: Authenticator

  override protected def registerAuthStrategies {
    scentry.register(RememberMe.name, app => new RememberMeStrategy(app, rememberMe, authenticator))
    scentry.register(UserPassword.name, app => new UserPasswordStrategy(app, login, password, authenticator))
  }

  protected def fromSession = {
    case id: String => {
      val userOpt: Option[User] = authenticator.findByLogin(id)
      userOpt match {
        case Some(u) => u
        case _ => null
      }
    }
  }

  protected def toSession = {
    case usr: User => usr.authentication.usernameLowerCase
  }

  override protected def configureScentry {
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
    if(!isAuthenticated) {
      halt(401, Map("error" -> "User not logged in"))
    }
  }

  def haltWithForbiddenIf(f: Boolean) {
    if (f) halt(403, Map("error" -> "Action forbidden"))
  }

  def haltIfNotCurrentUser(userId: ObjectId) = {
    haltIfNotAuthenticated()
    haltWithForbiddenIf(userId != user.id)
  }


}