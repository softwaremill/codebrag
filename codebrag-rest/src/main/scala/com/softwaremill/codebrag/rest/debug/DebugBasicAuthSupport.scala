package com.softwaremill.codebrag.rest.debug

import org.scalatra.auth.{ScentryConfig, ScentrySupport}
import org.scalatra.ScalatraBase
import org.scalatra.auth.strategy.{BasicAuthSupport, BasicAuthStrategy}

class DebugBasicAuthStrategy(protected override val app: ScalatraBase, realm: String,
                             login: String, password: String)
  extends BasicAuthStrategy[User](app, realm) {

  protected def validate(userName: String, password: String): Option[User] = {
    if (userName == this.login && password == this.password) Some(User("debugUser"))
    else None
  }

  protected def getUserId(user: User): String = user.id
}

trait DebugBasicAuthSupport extends ScentrySupport[User] with BasicAuthSupport[User] {

  self: ScalatraBase =>

  def login: String
  def password: String

  val realm = "Codebrag debug realm"

  protected def fromSession = {
    case id: String => User(id)
  }

  protected def toSession = {
    case usr: User => usr.id
  }

  protected val scentryConfig = new ScentryConfig {}.asInstanceOf[ScentryConfiguration]


  override protected def configureScentry {
    scentry.unauthenticated {
      scentry.strategies("Basic").unauthenticated()
    }
  }

  override protected def registerAuthStrategies {
    scentry.register("Basic", app => new DebugBasicAuthStrategy(app, realm, login, password))
  }
}

case class User(id: String)