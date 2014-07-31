package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec

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

  override def beforeEach {
    super.beforeEach
    userDao = mock[UserDAO]
    useCase = mock[ChangeUserSettingsUseCase]
    addServlet(new TestableUserSettingsServlet(fakeAuthenticator, fakeScentry), "/*")
  }

  "PUT /" should "update user settings" in {
    val currentUser = UserAssembler.randomUser.get
    userIsAuthenticatedAs(currentUser)
    val incomingSettingsJson = """{"emailNotificationsEnabled": true, "selectedBranch": "master"}"""
    val expectedSettings = IncomingSettings(Some(true), None, None)
    when(useCase.execute(currentUser.id, expectedSettings)).thenReturn(Right(UserSettings.defaults(currentUser.emailLowerCase)))
    put("/", incomingSettingsJson, defaultJsonHeaders) {
      status should be(200)
    }
  }

  class TestableUserSettingsServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[User])
    extends UsersSettingsServlet(fakeAuthenticator, userDao, useCase) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}
