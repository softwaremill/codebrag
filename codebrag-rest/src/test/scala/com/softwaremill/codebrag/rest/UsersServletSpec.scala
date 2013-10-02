package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec
import com.softwaremill.codebrag.service.user.{RegisterService, Authenticator}
import com.softwaremill.codebrag.service.user.UserJsonBuilder._
import org.scalatra.auth.Scentry
import com.softwaremill.codebrag.service.data.UserJson
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.json4s.JsonDSL._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.softwaremill.codebrag.dao.UserDAO

class UsersServletSpec extends AuthenticatableServletSpec {

  val registerService = mock[RegisterService]
  val userDao = mock[UserDAO]

  override def beforeEach {
    super.beforeEach
    addServlet(new TestableUsersServlet(fakeAuthenticator, fakeScentry), "/*")
  }

  "GET /logout" should "call logout() when user is already authenticated" in {
    userIsAuthenticated
    get("/logout") {
      verify(fakeScentry).logout()(any[HttpServletRequest], any[HttpServletResponse])
    }
  }

  "GET /logout" should "not call logout() when user is not authenticated" in {
    userIsNotAuthenticated
    get("/logout") {
      verify(fakeScentry, never).logout()(any[HttpServletRequest], any[HttpServletResponse])
      verifyZeroInteractions(fakeAuthenticator)
    }
  }

  "GET /" should "return user information" in {
    val currentUser = someUser()
    userIsAuthenticatedAs(currentUser)
    get("/") {
      status should be(200)
      body should be(asJson(currentUser))
    }
  }

  "GET /first-registration" should "return firstRegistration flag" in {
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
    when(registerService.register("adamw", "adam@example.org", "123456", "code")).thenReturn(Right(()))

    post("/register",
      mapToJson(Map("login" -> "adamw", "email" -> "adam@example.org", "password" -> "123456", "invitationCode" -> "code")),
      defaultJsonHeaders) {
      status should be(200)
    }
  }

  "POST /register" should "call the register service and return 403 if registration is unsuccessful" in {
    when(registerService.register("adamw", "adam@example.org", "123456", "code")).thenReturn(Left("error"))

    post("/register",
      mapToJson(Map("login" -> "adamw", "email" -> "adam@example.org", "password" -> "123456", "invitationCode" -> "code")),
      defaultJsonHeaders) {
      status should be(403)
    }
  }

  class TestableUsersServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[UserJson])
    extends UsersServlet(fakeAuthenticator, registerService, userDao, new CodebragSwagger) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}
