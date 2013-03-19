package com.softwaremill.codebrag

import org.scalatest.BeforeAndAfter
import service.data.UserJson
import service.user.Authenticator
import org.scalatra.auth.Scentry
import org.mockito.Mockito._

/**
 * Exposes fake authenticator and fake Scentry to specs so that it is possible to
 * do given test in context of authenticated or not authenticated user
 */
trait AuthenticatableServletSpec extends CodebragServletSpec with BeforeAndAfter {

  var fakeAuthenticator: Authenticator = null
  var fakeScentry: Scentry[UserJson] = null

  before {
    fakeAuthenticator = mock[Authenticator]
    fakeScentry = mock[Scentry[UserJson]]
    bindServlet
    beforeSpec
  }

  def bindServlet: Unit

  def beforeSpec: Unit = { /* noop by default */ }

  def userIsAuthenticated = when(fakeScentry.isAuthenticated).thenReturn(true)
  def userIsNotAuthenticated = when(fakeScentry.isAuthenticated).thenReturn(false)

}
