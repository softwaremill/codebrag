package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec
import com.softwaremill.codebrag.service.user.{AfterUserLogin, RegisterService, Authenticator}
import com.softwaremill.codebrag.service.user.UserJsonBuilder._
import org.scalatra.auth.Scentry
import com.softwaremill.codebrag.service.data.UserJson
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.json4s.JsonDSL._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.typesafe.config.ConfigFactory
import java.util.Properties
import com.softwaremill.codebrag.activities.{LoginUserUseCase, UserToRegister, RegisterNewUserUseCase}
import com.softwaremill.codebrag.dao.finders.user.{ManagedUserView, ManagedUsersListView, UserFinder}

class SessionServletSpec extends AuthenticatableServletSpec {

  val loginUserUseCase = mock[LoginUserUseCase]

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
    val currentUser = someUser
    userIsAuthenticatedAs(currentUser)
    get("/") {
      status should be(200)
      body should be(asJson(currentUser))
    }
  }

  def configWithDemo(mode: Boolean) = {
    val p = new Properties()
    p.setProperty("codebrag.demo", mode.toString)
    new CodebragConfig {
      def rootConfig = ConfigFactory.parseProperties(p)
    }
  }

  class TestableSessionServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[UserJson]) extends SessionServlet(fakeAuthenticator, loginUserUseCase) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}
