package com.softwaremill.codebrag.auth

import org.scalatra.SweetCookies
import javax.servlet.http.HttpServletResponse
import com.softwaremill.codebrag.rest.UsersServlet
import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.service.data.UserJson
import org.scalatra.test.scalatest.ScalatraFlatSpec
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.BDDMockito._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.Utils

class RememberMeStrategySpec extends ScalatraFlatSpec with MockitoSugar {
  behavior of "RememberMe"

  val httpResponse = mock[HttpServletResponse]
  val app = mock[UsersServlet]
  val userService = mock[Authenticator]
  val loggedUser: UserJson = UserJson(new ObjectId().toString, "admin", "admin@admin.net", "token", Utils.defaultAvatarUrl("admin@admin.net"))
  when(userService.authenticateWithToken(loggedUser.token)) thenReturn(Option(loggedUser))

  val rememberMe = true
  val strategy = new RememberMeStrategy(app, rememberMe, userService)

  it should "authenticate user base on cookie" in {
    // Given
    given(app.cookies) willReturn new SweetCookies(Map(("rememberMe", loggedUser.token)), httpResponse)

    // When
    val user: Option[UserJson] = strategy.authenticate()

    // Then
    user must not be (None)
    user.get.login must be ("admin")
  }

  it should "not authenticate user with invalid cookie" in {
    // Given
    given(app.cookies) willReturn new SweetCookies(Map(("rememberMe", loggedUser.token + "X")), httpResponse)

    // When
    val user: Option[UserJson] = strategy.authenticate()

    // Then
    user must be (null)
  }
}
