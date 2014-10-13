package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec
import com.softwaremill.scalaval.Validation

import org.scalatest.BeforeAndAfterEach
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra.auth.Scentry
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.{User, UserSettings}
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.usecases.user.{IncomingSettings, ChangeUserSettingsUseCase}


class UserSettingsServletSpec extends AuthenticatableServletSpec with BeforeAndAfterEach {

  val authenticator: Authenticator = mock[Authenticator]
  var userDao: UserDAO = _
  var useCase: ChangeUserSettingsUseCase = _

  var currentUser: User = _

  override def beforeEach {
    super.beforeEach
    userDao = mock[UserDAO]
    useCase = mock[ChangeUserSettingsUseCase]
    addServlet(new TestableUserSettingsServlet(fakeAuthenticator, fakeScentry), "/*")

    currentUser = UserAssembler.randomUser.get
    userIsAuthenticatedAs(currentUser)
  }

  "PUT /" should "update user settings" in {
    // given
    val incomingSettings = IncomingSettings(Some(true), None, None)
    val incomingSettingsJson = asJson(incomingSettings)
    val useCaseReturnedSettings = UserSettings.defaults(currentUser.emailLowerCase)
    when(useCase.execute(currentUser.id, incomingSettings)).thenReturn(Right(useCaseReturnedSettings))

    // when
    put("/", incomingSettingsJson, defaultJsonHeaders) {
      // then
      body should be(asJson(Map("userSettings" -> useCaseReturnedSettings)))
      status should be(200)
    }
  }

  "PUT /" should "not update user settings when validation error occurs" in {
    // given
    val incomingSettings = IncomingSettings(Some(true), None, None)
    val incomingSettingsJson = asJson(incomingSettings)
    when(useCase.execute(currentUser.id, incomingSettings)).thenReturn(Left("Use case execution failed"))

    // when
    put("/", incomingSettingsJson, defaultJsonHeaders) {
      // then
      body should be(asJson(Map("error" -> "Use case execution failed")))
      status should be(400)
    }
  }

  "GET /" should "return current user settings" in {
    // given
    when(userDao.findById(currentUser.id)).thenReturn(Some(currentUser))

    // when
    get("/") {
      // then
      body should be(asJson(Map("userSettings" -> currentUser.settings)))
      status should be(200)
    }
  }

  "GET /" should "return NotFound if user does not exist (weird case as user is taken from session, but well...)" in {
    // given
    when(userDao.findById(currentUser.id)).thenReturn(None)

    // when
    get("/") {
      // then
      status should be(404)
    }
  }

  class TestableUserSettingsServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[User])
    extends UsersSettingsServlet(fakeAuthenticator, userDao, useCase) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}
