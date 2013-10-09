package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec

import com.softwaremill.codebrag.service.config.CodebragConfig
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterEach
import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.service.user.Authenticator
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.builder.UserAssembler
import org.scalatra.auth.Scentry
import com.softwaremill.codebrag.service.data.UserJson


class ConfigServletSpec extends AuthenticatableServletSpec with BeforeAndAfterEach {

  val userDao: UserDAO = mock[UserDAO]
  val authenticator: Authenticator = mock[Authenticator]
  var config: CodebragConfig = _

  override def beforeEach {
    super.beforeEach
    config = new CodebragConfig {
      def rootConfig = ConfigFactory.load("test.conf")
    }
    addServlet(new TestableConfigServlet(fakeAuthenticator, fakeScentry), "/*")
  }

  "GET /" should "return demo flag" in {
    get("/") {
      status should be(200)
      body should be( """{"demo":true}""")
    }
  }

  "GET /user" should "return current user's configuration" in {
    //given
    val user = UserAssembler.randomUser.get
    userIsAuthenticatedAs(UserJson(user))
    given(userDao.findById(user.id)).willReturn(Option(user))

    //when
    get("/user") {
      //then
      status should equal(200)
      body should include( """"emailNotifications":true""")
    }
  }

  "GET /user" should "return 401 when user is not logged in" in {
    //given
    userIsNotAuthenticated

    //when
    get("/user") {
      //then
      status should equal(401)
    }
  }

  "PUT /user" should "update user's email notifications setting" in {
    //give
    val user = UserAssembler.randomUser.get
    userIsAuthenticatedAs(UserJson(user))
    val notificationsEnabled = true

    //when
    val json = asJson(Map("emailNotifications" -> notificationsEnabled))
    put("/user", json, defaultJsonHeaders) {
      //then
      verify(userDao).changeEmailNotifications(user.id, emailNotificationsEnabled = notificationsEnabled)
    }
  }

  "PUT /user" should "return 401 when user is not logged in" in {
    //given
    userIsNotAuthenticated
    val notificationsEnabled = true

    //when
    val json = asJson(Map("emailNotifications" -> notificationsEnabled))
    put("/user", json, defaultJsonHeaders) {
      //then
      status should equal(401)
    }
  }

  class TestableConfigServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[UserJson])
    extends ConfigServlet(config, userDao, fakeAuthenticator) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}
