package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.{AuthenticatableServletSpec, CodebragServletSpec}
import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra.auth.Scentry
import com.softwaremill.codebrag.service.data.UserJson
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import com.softwaremill.codebrag.auth.AuthenticationSupport
import org.scalatra.{Ok, ScalatraBase}
import org.scalatest.BeforeAndAfter
import com.softwaremill.codebrag.dao.CommitInfoDAO


class CommitsServletSpec extends AuthenticatableServletSpec {

  var commitsInfoDao = mock[CommitInfoDAO]

  def bindServlet = addServlet(new TestableCommitsServlet(commitsInfoDao, fakeAuthenticator, fakeScentry), "/*")

  "GET /commits" should "respond with HTTP 401 when user is not authenticated" in {
    userIsNotAuthenticated
    get("/") {
      status should be (401)
    }
  }

  "GET /commits" should "respond with HTTP 404 (not yet done) when user is authenticated" in {
    userIsAuthenticated
    get("/") {
      status should be (404)
    }
  }

  "GET /commits?type=pending" should "respond with empty HTTP 200 when user is authenticated" in {
    userIsAuthenticated
    get("/?type=pending") {
      status should be (200)
    }
  }

  class TestableCommitsServlet(commitInfoDao: CommitInfoDAO, fakeAuthenticator: Authenticator, fakeScentry: Scentry[UserJson]) extends CommitsServlet(fakeAuthenticator, commitsInfoDao, new CodebragSwagger) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}

