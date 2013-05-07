package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.AuthenticatableServletSpec
import org.scalatra.auth.Scentry
import com.softwaremill.codebrag.service.data.UserJson
import com.softwaremill.codebrag.dao.{CommitReviewTaskDAO, UserDAO, CommitInfoDAO}
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.softwaremill.codebrag.dao.reporting.{CommentFinder, CommitFinder}
import java.util.Date
import com.softwaremill.codebrag.service.diff.{DiffWithCommentsService, DiffService}
import com.softwaremill.codebrag.service.github.GitHubCommitImportServiceFactory
import com.softwaremill.codebrag.activities.AddCommentActivity
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.CommitReviewTask
import com.softwaremill.codebrag.dao.reporting.views.{CommitView, CommitListView}
import org.mockito.Matchers


class CommitsServletSpec extends AuthenticatableServletSpec {

  val SamplePendingCommits = CommitListView(List(CommitView("id", "abcd0123", "this is commit message", "mostr", "michal", new Date())))
  var commentActivity = mock[AddCommentActivity]
  var commitsInfoDao = mock[CommitInfoDAO]
  var commitsListFinder = mock[CommitFinder]
  var diffService = mock[DiffWithCommentsService]
  var commentListFinder = mock[CommentFinder]
  var userDao = mock[UserDAO]
  var commitReviewTaskDao = mock[CommitReviewTaskDAO]

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

  "GET /commits" should "should return commits pending review" in {
    val userId = new ObjectId
    val user = UserJson(userId.toString, "user", "user@email.com", "token")
    userIsAuthenticatedAs(user)
    when(commitsListFinder.findCommitsToReviewForUser(userId)).thenReturn(SamplePendingCommits)
    get("/") {
      status should be(200)
      body should equal(asJson(SamplePendingCommits))
    }
  }

  "GET /commits?reviewed=false" should "should return commits pending review" in {
    val userId = new ObjectId
    val user = UserJson(userId.toString, "user", "user@email.com", "token")
    userIsAuthenticatedAs(user)
    when(commitsListFinder.findCommitsToReviewForUser(userId)).thenReturn(SamplePendingCommits)
    get("/?filter=pending") {
      status should be(200)
      body should equal(asJson(SamplePendingCommits))
    }
  }

  "GET /commits?reviewed=true" should "should return all commits" in {
    val userId = new ObjectId
    val user = UserJson(userId.toString, "user", "user@email.com", "token")
    userIsAuthenticatedAs(user)
    when(commitsListFinder.findAll(userId)).thenReturn(SamplePendingCommits)
    get("/?filter=all") {
      status should be(200)
      body should equal(asJson(SamplePendingCommits))
      verify(commitsListFinder, never()).findCommitInfoById(anyString(), Matchers.eq(userId))
    }
  }

  "DELETE /commits/:id" should "should remove commits from review list" in {
    val userId = new ObjectId
    val commitId = new ObjectId
    val user = UserJson(userId.toString, "user", "user@email.com", "token")
    userIsAuthenticatedAs(user)
    delete(s"/$commitId") {
      verify(commitReviewTaskDao).delete(CommitReviewTask(commitId, userId))
      status should be(200)
    }
  }

  class TestableCommitsServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[UserJson])
    extends CommitsServlet(fakeAuthenticator, commitsListFinder, commentListFinder, commentActivity, commitReviewTaskDao, userDao, new CodebragSwagger, diffService, importerFactory) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}