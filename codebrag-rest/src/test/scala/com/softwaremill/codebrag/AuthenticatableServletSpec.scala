package com.softwaremill.codebrag

import org.scalatest.BeforeAndAfter
import service.data.UserJson
import service.user.Authenticator
import org.scalatra.auth.Scentry
import org.mockito.Mockito._
import org.mockito.Matchers._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

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

  def userIsAuthenticated = when(fakeScentry.isAuthenticated(any[HttpServletRequest], any[HttpServletResponse])).thenReturn(true)

  def userIsAuthenticatedAs(currentUser: UserJson) = {
    userIsAuthenticated
    when(fakeScentry.user(any[HttpServletRequest], any[HttpServletResponse])).thenReturn(currentUser)
  }

  def userIsNotAuthenticated = when(fakeScentry.isAuthenticated(any[HttpServletRequest], any[HttpServletResponse])).thenReturn(false)

}
