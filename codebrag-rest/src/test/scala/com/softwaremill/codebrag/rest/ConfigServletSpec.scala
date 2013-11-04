package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec

import com.softwaremill.codebrag.service.config.CodebragConfig
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterEach
import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.service.user.Authenticator
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

  "GET /" should "return system wide notifications" in {
    get("/") {
      status should be(200)
      body should be( """{"demo":true,"emailNotifications":true}""")
    }
  }

  class TestableConfigServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[UserJson])
    extends ConfigServlet(config, fakeAuthenticator) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}
