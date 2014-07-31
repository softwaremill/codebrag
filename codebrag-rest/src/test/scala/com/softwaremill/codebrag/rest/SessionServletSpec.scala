package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec
import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra.auth.Scentry
import org.mockito.Mockito._
import org.mockito.Matchers._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.typesafe.config.ConfigFactory
import java.util.Properties
import com.softwaremill.codebrag.finders.user.{LoggedInUserView, UserFinder}
import com.softwaremill.codebrag.finders.browsingcontext.UserBrowsingContext
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.usecases.user.LoginUserUseCase

class SessionServletSpec extends AuthenticatableServletSpec {

  val loginUserUseCase = mock[LoginUserUseCase]
  val userFinder = mock[UserFinder]

  "DELETE /" should "call logout() when user is already authenticated" in {
    addServlet(new TestableSessionServlet(fakeAuthenticator, fakeScentry), "/*")
    userIsAuthenticated
    delete("/") {
      verify(fakeScentry).logout()(any[HttpServletRequest], any[HttpServletResponse])
    }
  }

  "DELETE /" should "not call logout() when user is not authenticated" in {
    addServlet(new TestableSessionServlet(fakeAuthenticator, fakeScentry), "/*")
    userIsNotAuthenticated
    delete("/") {
      verify(fakeScentry, never).logout()(any[HttpServletRequest], any[HttpServletResponse])
      verifyZeroInteractions(fakeAuthenticator)
    }
  }

  "GET /" should "return user information" in {
    addServlet(new TestableSessionServlet(fakeAuthenticator, fakeScentry), "/*")
    val currentUser = UserAssembler.randomUser.get
    userIsAuthenticatedAs(currentUser)
    val loggedInUserView = LoggedInUserView(currentUser, UserBrowsingContext(currentUser.id, "codebrag", "master"))
    when(userFinder.findLoggedInUser(currentUser)).thenReturn(loggedInUserView)
    get("/") {
      status should be(200)
      body should be(asJson(loggedInUserView))
    }
  }

  def configWithDemo(mode: Boolean) = {
    val p = new Properties()
    p.setProperty("codebrag.demo", mode.toString)
    new CodebragConfig {
      def rootConfig = ConfigFactory.parseProperties(p)
    }
  }

  class TestableSessionServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[User]) extends SessionServlet(fakeAuthenticator, loginUserUseCase, userFinder) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}
