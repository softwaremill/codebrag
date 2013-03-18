package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.CodebragServletSpec
import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra.auth.Scentry
import com.softwaremill.codebrag.service.data.UserJson
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

class UsersServletWithAuthSpec extends CodebragServletSpec {

  def onServletWithMocks(authenticated: Boolean, testToExecute: (Authenticator, Scentry[UserJson]) => Unit) {
    val authenticator = mock[Authenticator]

    val mockedScentry = mock[Scentry[UserJson]]
    when(mockedScentry.isAuthenticated) thenReturn authenticated

    val servlet: MockUsersServlet = new MockUsersServlet(authenticator, mockedScentry)
    addServlet(servlet, "/*")

    testToExecute(authenticator, mockedScentry)
  }

  "GET /logout" should "call logout() when user is already authenticated" in {
    onServletWithMocks(authenticated = true, testToExecute = (authenticator, mock) =>
      get("/logout") {
        verify(mock, times(2)).isAuthenticated // before() and get('/logout')
        verify(mock).logout()
        verifyZeroInteractions(authenticator)
      }
    )
  }

  "GET /logout" should "not call logout() when user is not authenticated" in {
    onServletWithMocks(authenticated = false, testToExecute = (authenticator, mock) =>
      get("/logout") {
        verify(mock, times(2)).isAuthenticated // before() and get('/logout')
        verify(mock, never).logout()
        verifyZeroInteractions(authenticator)
      }
    )
  }

  "GET /" should "return user information" in {
    onServletWithMocks(authenticated = true, testToExecute = (authenticator, mock) =>
      get("/") {
        status should be (200)
        body should be ("{\"login\":\"Jas Kowalski\",\"email\":\"kowalski@kowalski.net\",\"token\":\"token\"}")
      }
    )
  }

  class MockUsersServlet(authenticator: Authenticator, mockedScentry: Scentry[UserJson]) extends UsersServlet(authenticator, new CodebragSwagger) with MockitoSugar {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = mockedScentry
    override def user(implicit request: javax.servlet.http.HttpServletRequest) = new UserJson("Jas Kowalski", "kowalski@kowalski.net", "token")
  }
}

