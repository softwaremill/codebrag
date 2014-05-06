package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec
import com.softwaremill.codebrag.service.user.{AfterUserLoginHook, RegisterService, Authenticator}
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
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.cache.UserReviewedCommitsCache
import com.softwaremill.codebrag.activities.{UserToRegister, RegisterNewUserUseCase}

class UsersServletSpec extends AuthenticatableServletSpec {

  val registerService = mock[RegisterService]
  val registerUseCase = mock[RegisterNewUserUseCase]
  val afterUserLoginHook = mock[AfterUserLoginHook]
  var userDao: UserDAO = _
  var config: CodebragConfig = _

  override def beforeEach() {
    super.beforeEach()
    userDao = mock[UserDAO]
    config = configWithDemo(false)
  }

  "GET /logout" should "call logout() when user is already authenticated" in {
    addServlet(new TestableUsersServlet(fakeAuthenticator, fakeScentry), "/*")
    userIsAuthenticated
    get("/logout") {
      verify(fakeScentry).logout()(any[HttpServletRequest], any[HttpServletResponse])
    }
  }

  "GET /logout" should "not call logout() when user is not authenticated" in {
    addServlet(new TestableUsersServlet(fakeAuthenticator, fakeScentry), "/*")
    userIsNotAuthenticated
    get("/logout") {
      verify(fakeScentry, never).logout()(any[HttpServletRequest], any[HttpServletResponse])
      verifyZeroInteractions(fakeAuthenticator)
    }
  }

  "GET /" should "return user information" in {
    addServlet(new TestableUsersServlet(fakeAuthenticator, fakeScentry), "/*")
    val currentUser = someUser()
    userIsAuthenticatedAs(currentUser)
    get("/") {
      status should be(200)
      body should be(asJson(currentUser))
    }
  }

  "GET /all" should "return empty list of registered users if in demo mode" in {
    config = configWithDemo(true)
    addServlet(new TestableUsersServlet(fakeAuthenticator, fakeScentry), "/*")
    userIsAuthenticatedAs(someUser())
    get("/all") {
      status should be(200)
      val expectedBody = Map("registeredUsers" -> List.empty)
      body should be(asJson(expectedBody))
    }
  }

  "GET /all" should "return actual list of registered users if not in demo mode" in {
    val actualUsers = List(UserAssembler.randomUser.withEmail("john@codebrag.com").get)
    when(userDao.findAll()).thenReturn(actualUsers)
    addServlet(new TestableUsersServlet(fakeAuthenticator, fakeScentry), "/*")
    userIsAuthenticatedAs(someUser())
    get("/all") {
      status should be(200)
      val expectedBody = Map("registeredUsers" -> actualUsers.map{user => Map("name" -> user.name, "email" -> user.emailLowerCase)})
      body should be(asJson(expectedBody))
    }
  }


  def configWithDemo(mode: Boolean) = {
    val p = new Properties()
    p.setProperty("codebrag.demo", mode.toString)
    new CodebragConfig {
      def rootConfig = ConfigFactory.parseProperties(p)
    }
  }

  "GET /first-registration" should "return firstRegistration flag" in {
    addServlet(new TestableUsersServlet(fakeAuthenticator, fakeScentry), "/*")
    //given
    val currentUser = someUser()
    userIsAuthenticatedAs(currentUser)
    when(registerService.firstRegistration).thenReturn(true)
    //when
    get("/first-registration") {
      //then
      status should be(200)
      body should be("{\"firstRegistration\":true}")
    }
  }

  "POST /register" should "call the register service and return 200 if registration is successful" in {
    addServlet(new TestableUsersServlet(fakeAuthenticator, fakeScentry), "/*")
    val newUser = UserToRegister("adamw", "adam@example.org", "123456", "code")
    when(registerUseCase.execute(newUser)).thenReturn(Right())

    post("/register",
      mapToJson(Map("login" -> "adamw", "email" -> "adam@example.org", "password" -> "123456", "invitationCode" -> "code")),
      defaultJsonHeaders) {
      status should be(200)
    }
  }

  "POST /register" should "call the register service and return 403 if registration is unsuccessful" in {
    addServlet(new TestableUsersServlet(fakeAuthenticator, fakeScentry), "/*")
    val newUser = UserToRegister("adamw", "adam@example.org", "123456", "code")
    when(registerUseCase.execute(newUser)).thenReturn(Left("error"))

    post("/register",
      mapToJson(Map("login" -> "adamw", "email" -> "adam@example.org", "password" -> "123456", "invitationCode" -> "code")),
      defaultJsonHeaders) {
      status should be(403)
    }
  }

  class TestableUsersServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[UserJson])
    extends UsersServlet(fakeAuthenticator, registerService, registerUseCase, afterUserLoginHook, userDao, config, new CodebragSwagger) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}
