package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.service.user.UserJsonBuilder._
import com.softwaremill.codebrag.AuthenticatableServletSpec
import org.scalatra.auth.Scentry
import com.softwaremill.codebrag.service.data.UserJson
import com.softwaremill.codebrag.dao.{CommitReviewTaskDAO, UserDAO, CommitInfoDAO}
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.softwaremill.codebrag.dao.reporting.{UserReactionFinder, CommitFinder}
import java.util.Date
import com.softwaremill.codebrag.service.diff.DiffWithCommentsService
import com.softwaremill.codebrag.service.github.GitHubCommitImportServiceFactory
import com.softwaremill.codebrag.activities.AddCommentActivity
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.CommitReviewTask
import com.softwaremill.codebrag.dao.reporting.views.{CommitView, CommitListView}
import org.mockito.Matchers
import com.softwaremill.codebrag.common.PagingCriteria
import com.softwaremill.codebrag.service.comments.UserReactionService


class CommitsServletSpec extends AuthenticatableServletSpec {

  val SamplePendingCommits = CommitListView(List(CommitView("id", "abcd0123", "this is commit message", "mostr", new Date())), 1)
  var commentActivity = mock[AddCommentActivity]
  var commitsInfoDao = mock[CommitInfoDAO]
  var commitsListFinder = mock[CommitFinder]
  var diffService = mock[DiffWithCommentsService]
  var userReactionFinder = mock[UserReactionFinder]
  var userDao = mock[UserDAO]
  var commitReviewTaskDao = mock[CommitReviewTaskDAO]
  val UserJson = someUser()
  val importerFactory = mock[GitHubCommitImportServiceFactory]
  val userReactionService = mock[UserReactionService]

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
    val userId = givenStandardAuthenticatedUser()
    when(commitsListFinder.findCommitsToReviewForUser(userId, PagingCriteria(0, 7))).thenReturn(SamplePendingCommits)
    get("/") {
      status should be(200)
      body should equal(asJson(SamplePendingCommits))
    }
  }

  "GET /commits?skip=-1" should "should return error status with message" in {
    givenStandardAuthenticatedUser()
    // when
    get("/?skip=-1") {
      status should be(400)
      body should equal("skip value must be non-negative")
    }
  }

  "GET /commits?limit=0" should "should return error status with message" in {
    givenStandardAuthenticatedUser()
    // when
    get("/?limit=0") {
      status should be(400)
      body should equal("limit value must be positive")
    }
  }

    "GET /commits?skip=5" should "should query for commits with proper skip value" in {
    val userId = givenStandardAuthenticatedUser()
    when(commitsListFinder.findCommitsToReviewForUser(userId, PagingCriteria(5, 7))).thenReturn(SamplePendingCommits)
    get("/?skip=5") {
      status should be(200)
      body should equal(asJson(SamplePendingCommits))
    }
  }

  "GET /commits?limit=22" should "should query for commits with proper limit value" in {
    val userId = givenStandardAuthenticatedUser()
    when(commitsListFinder.findCommitsToReviewForUser(userId, PagingCriteria(0, 22))).thenReturn(SamplePendingCommits)
    get("/?limit=22") {
      status should be(200)
      body should equal(asJson(SamplePendingCommits))
    }
  }

  "GET /commits?limit=22&skip=21" should "should query for commits with proper limit and skip values" in {
    val userId = givenStandardAuthenticatedUser()
    when(commitsListFinder.findCommitsToReviewForUser(userId, PagingCriteria(21, 22))).thenReturn(SamplePendingCommits)
    get("/?limit=22&skip=21") {
      status should be(200)
      body should equal(asJson(SamplePendingCommits))
    }
  }

  "GET /commits?filter=pending" should "should return commits pending review" in {
    val userId = givenStandardAuthenticatedUser()
    when(commitsListFinder.findCommitsToReviewForUser(userId, PagingCriteria(0, 7))).thenReturn(SamplePendingCommits)

    get("/?filter=pending") {
      status should be(200)
      body should equal(asJson(SamplePendingCommits))
    }
  }

  "GET /commits?filter=all" should "should return all commits" in {
    val userId = givenStandardAuthenticatedUser()
    when(commitsListFinder.findAll(userId)).thenReturn(SamplePendingCommits)
    get("/?filter=all") {
      status should be(200)
      body should equal(asJson(SamplePendingCommits))
      verify(commitsListFinder, never()).findCommitInfoById(anyString(), Matchers.eq(userId))
    }
  }

  "DELETE /commits/:id" should "should remove commits from review list" in {
    val userId = givenStandardAuthenticatedUser()
    val commitId = new ObjectId
    delete(s"/$commitId") {
      verify(commitReviewTaskDao).delete(CommitReviewTask(commitId, userId))
      status should be(200)
    }
  }

  private def givenStandardAuthenticatedUser(): ObjectId = {
    val user = UserJson
    userIsAuthenticatedAs(user)
    new ObjectId(user.id)
  }

  class TestableCommitsServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[UserJson])
    extends CommitsServlet(fakeAuthenticator, commitsListFinder, userReactionFinder, commentActivity, commitReviewTaskDao, userReactionService, userDao, new CodebragSwagger, diffService, importerFactory) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}