package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec

import org.scalatest.BeforeAndAfterEach
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra.auth.Scentry
import com.softwaremill.codebrag.service.data.UserJson
import com.softwaremill.codebrag.usecases.{IncomingSettings, ChangeUserSettingsUseCase}
import com.softwaremill.codebrag.service.user.UserJsonBuilder._
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.UserSettings


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
    val currentUser = someUser
    userIsAuthenticatedAs(currentUser)
    val incomingSettingsJson = """{"emailNotificationsEnabled": true, "selectedBranch": "master"}"""
    val expectedSettings = IncomingSettings(Some(true), None, None)
    when(useCase.execute(currentUser.idAsObjectId, expectedSettings)).thenReturn(Right(UserSettings.defaults(currentUser.email  )))
    put("/", incomingSettingsJson, defaultJsonHeaders) {
      status should be(200)
    }
  }

  class TestableUserSettingsServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[UserJson])
    extends UsersSettingsServlet(fakeAuthenticator, userDao, useCase) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}
