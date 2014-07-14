package com.softwaremill.codebrag.auth

import org.scalatra.SweetCookies
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.softwaremill.codebrag.rest.UsersServlet
import com.softwaremill.codebrag.service.user.Authenticator

import org.scalatra.test.scalatest.ScalatraFlatSpec
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.BDDMockito._
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.domain.User

class RememberMeStrategySpec extends ScalatraFlatSpec with MockitoSugar {
  behavior of "RememberMe"

  val httpResponse = mock[HttpServletResponse]
  val httpRequest = mock[HttpServletRequest]
  val app = mock[UsersServlet]
  val userService = mock[Authenticator]
  val loggedUser = UserAssembler.randomUser.get
  when(userService.authenticateWithToken(loggedUser.token)) thenReturn(Option(loggedUser))

  val rememberMe = true
  val strategy = new RememberMeStrategy(app, rememberMe, userService) {
    override def cookieKey(implicit request: HttpServletRequest): String = "rememberMe"
  }

  it should "authenticate user base on cookie" in {
    // Given
    given(app.cookies) willReturn new SweetCookies(Map(("rememberMe", loggedUser.token)), httpResponse)

    // When
    val user = strategy.authenticate()(httpRequest, httpResponse)

    // Then
    user must not be (None)
    user.get.authentication.username must be (loggedUser.authentication.username)
  }

  it should "not authenticate user with invalid cookie" in {
    // Given
    given(app.cookies) willReturn new SweetCookies(Map(("rememberMe", loggedUser.token + "X")), httpResponse)

    // When
    val user: Option[User] = strategy.authenticate()(httpRequest, httpResponse)

    // Then
    user must be (null)
  }
}
