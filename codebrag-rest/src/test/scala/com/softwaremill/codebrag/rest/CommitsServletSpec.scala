package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec
import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra.auth.Scentry
import com.softwaremill.codebrag.service.data.UserJson
import com.softwaremill.codebrag.dao.CommitInfoDAO
import com.softwaremill.codebrag.domain.CommitInfo
import org.mockito.Mockito._
import org.joda.time.DateTime


class CommitsServletSpec extends AuthenticatableServletSpec {

  val SamplePendingCommits = List(CommitInfo("abcd0123", "this is commit message", "mostr", "michal", new DateTime(), List("abc00001")))

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

  "GET /commits?type=pending" should "should return commits pending review" in {
    userIsAuthenticated
    when(commitsInfoDao.findAllPendingCommits()).thenReturn(SamplePendingCommits)
    get("/?type=pending") {
      status should be (200)
      body should equal(asJson(CommitsResponse(SamplePendingCommits)))
    }

    def asJson(resp: CommitsResponse) = {
      implicit val formats = net.liftweb.json.DefaultFormats
      net.liftweb.json.Serialization.write(resp)
    }
  }

  class TestableCommitsServlet(commitInfoDao: CommitInfoDAO, fakeAuthenticator: Authenticator, fakeScentry: Scentry[UserJson]) extends CommitsServlet(fakeAuthenticator, commitsInfoDao, new CodebragSwagger) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}



