package com.softwaremill.codebrag

import org.scalatest.BeforeAndAfterEach
import service.user.Authenticator
import org.scalatra.auth.Scentry
import org.mockito.Mockito._
import org.mockito.Matchers._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.softwaremill.codebrag.domain.User

/**
 * Exposes fake authenticator and fake Scentry to specs so that it is possible to
 * do given test in context of authenticated or not authenticated user
 */
trait AuthenticatableServletSpec extends CodebragServletSpec with BeforeAndAfterEach {

  var fakeAuthenticator: Authenticator = null
  var fakeScentry: Scentry[User] = null

  override def beforeEach {
    fakeAuthenticator = mock[Authenticator]
    fakeScentry = mock[Scentry[User]]
  }

  def userIsAuthenticated = when(fakeScentry.isAuthenticated(any[HttpServletRequest], any[HttpServletResponse])).thenReturn(true)

  def userIsAuthenticatedAs(currentUser: User) = {
    userIsAuthenticated
    when(fakeScentry.user(any[HttpServletRequest], any[HttpServletResponse])).thenReturn(currentUser)
  }

  def userIsNotAuthenticated = when(fakeScentry.isAuthenticated(any[HttpServletRequest], any[HttpServletResponse])).thenReturn(false)

}
