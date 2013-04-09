package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.AuthenticatableServletSpec
import org.scalatra.auth.Scentry
import com.softwaremill.codebrag.service.data.UserJson
import com.softwaremill.codebrag.dao.{UserDAO, CommitInfoDAO}
import org.mockito.Mockito._
import com.softwaremill.codebrag.dao.reporting.{CommentListFinder, CommitListDTO, CommitListFinder, CommitListItemDTO}
import java.util.Date
import com.softwaremill.codebrag.service.diff.DiffService
import com.softwaremill.codebrag.service.github.GitHubCommitImportServiceFactory
import com.softwaremill.codebrag.activities.CommentActivity


class CommitsServletSpec extends AuthenticatableServletSpec {

  val SamplePendingCommits = CommitListDTO(List(CommitListItemDTO("id", "abcd0123", "this is commit message", "mostr", "michal", new Date())))
  var commentActivity = mock[CommentActivity]
  var commitsInfoDao = mock[CommitInfoDAO]
  var commitsListFinder = mock[CommitListFinder]
  var diffService = mock[DiffService]
  var commentListFinder = mock[CommentListFinder]
  var userDao = mock[UserDAO]

  val importerFactory = mock[GitHubCommitImportServiceFactory]

  def bindServlet {
    addServlet(new TestableCommitsServlet(fakeAuthenticator, fakeScentry), "/*")
  }

  "GET /commits" should "respond with HTTP 401 when user is not authenticated" in {
    userIsNotAuthenticated
    get("/") {
      status should be(401)
    }
  }

  "GET /commits" should "respond with HTTP 404 (not yet done) when user is authenticated" in {
    userIsAuthenticated
    get("/") {
      status should be(404)
    }
  }

  "GET /commits?type=pending" should "should return commits pending review" in {
    userIsAuthenticated
    when(commitsListFinder.findAllPendingCommits()).thenReturn(SamplePendingCommits)
    get("/?type=pending") {
      status should be(200)
      body should equal(asJson(SamplePendingCommits))
    }
  }

  class TestableCommitsServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[UserJson])
    extends CommitsServlet(fakeAuthenticator, commitsListFinder, commentListFinder, commentActivity, userDao, new CodebragSwagger, diffService, importerFactory) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}