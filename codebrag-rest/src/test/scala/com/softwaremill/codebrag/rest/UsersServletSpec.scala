package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec
import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra.auth.Scentry
import org.mockito.Mockito._
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.typesafe.config.ConfigFactory
import java.util.Properties
import com.softwaremill.codebrag.dao.ObjectIdTestUtils
import com.softwaremill.codebrag.finders.user.UserFinder
import com.softwaremill.codebrag.finders.user.ManagedUserView
import com.softwaremill.codebrag.finders.user.ManagedUsersListView
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.usecases.user.{ ModifyUserDetailsUseCase, DeleteUserUseCase }

class UsersServletSpec extends AuthenticatableServletSpec {

  val modifyUserUseCase = mock[ModifyUserDetailsUseCase]
  val deleteUserUseCase = mock[DeleteUserUseCase]
  var userFinder: UserFinder = _
  var config: CodebragConfig = _

  override def beforeEach() {
    super.beforeEach()
    userFinder = mock[UserFinder]
    config = configWithDemo(false)
  }

  "GET /" should "return empty list of registered users if in demo mode" in {
    config = configWithDemo(true)
    addServlet(new TestableUsersServlet(fakeAuthenticator, fakeScentry), "/*")
    userIsAuthenticatedAs(UserAssembler.randomUser.get)
    get("/") {
      status should be(200)
      val expectedBody = ManagedUsersListView(users = List.empty)
      body should be(asJson(expectedBody))
    }
  }

  "GET /" should "return actual list of registered users if not in demo mode" in {
    val user = ManagedUserView(ObjectIdTestUtils.oid(100), "john@doe.com", "John Doe", active = true, admin = true)
    val registeredUsers = ManagedUsersListView(List(user))
    when(userFinder.findAllAsManagedUsers()).thenReturn(registeredUsers)
    addServlet(new TestableUsersServlet(fakeAuthenticator, fakeScentry), "/*")
    userIsAuthenticatedAs(UserAssembler.randomUser.get)
    get("/") {
      status should be(200)
      body should be(asJson(registeredUsers))
    }
  }

  def configWithDemo(mode: Boolean) = {
    val p = new Properties()
    p.setProperty("codebrag.demo", mode.toString)
    new CodebragConfig {
      def rootConfig = ConfigFactory.parseProperties(p)
    }
  }

  class TestableUsersServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[User])
    extends UsersServlet(fakeAuthenticator, userFinder, modifyUserUseCase, deleteUserUseCase, config) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}
