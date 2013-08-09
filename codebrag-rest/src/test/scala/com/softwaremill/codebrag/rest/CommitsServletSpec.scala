package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.service.user.UserJsonBuilder._
import com.softwaremill.codebrag.AuthenticatableServletSpec
import org.scalatra.auth.Scentry
import com.softwaremill.codebrag.service.data.UserJson
import com.softwaremill.codebrag.dao.{CommitReviewTaskDAO, UserDAO, CommitInfoDAO}
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.softwaremill.codebrag.dao.reporting.{MongoReactionFinder, CommitFinder}
import java.util.Date
import com.softwaremill.codebrag.service.diff.DiffWithCommentsService
import com.softwaremill.codebrag.activities.AddCommentActivity
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.CommitReviewTask
import com.softwaremill.codebrag.dao.reporting.views.{CommitView, CommitListView}
import org.mockito.Matchers
import com.softwaremill.codebrag.common.{LoadSurroundingsCriteria, LoadMoreCriteria}
import com.softwaremill.codebrag.service.comments.UserReactionService
import com.softwaremill.codebrag.usecase.UnlikeUseCaseFactory


class CommitsServletSpec extends AuthenticatableServletSpec {

  val SamplePendingCommits = CommitListView(List(CommitView("id", "abcd0123", "this is commit message", "mostr",
    "mostr@sml.com", new Date())), 1)
  var commentActivity = mock[AddCommentActivity]
  var commitsInfoDao = mock[CommitInfoDAO]
  var commitsListFinder = mock[CommitFinder]
  var diffService = mock[DiffWithCommentsService]
  var userReactionFinder = mock[MongoReactionFinder]
  var userDao = mock[UserDAO]
  var commitReviewTaskDao = mock[CommitReviewTaskDAO]
  val UserJson = someUser()
  val userReactionService = mock[UserReactionService]
  val unlikeUseCaseFactory = mock[UnlikeUseCaseFactory]

  def bindServlet {
    addServlet(new TestableCommitsServlet(fakeAuthenticator, fakeScentry), "/*")
  }

  "GET /commits" should "respond with HTTP 401 when user is not authenticated" in {
    userIsNotAuthenticated
    get("/") {
      status should be(401)
    }
  }

  "GET /commits" should "return commits pending review" in {
    val userId = givenStandardAuthenticatedUser()
    when(commitsListFinder.findCommitsToReviewForUser(userId, LoadMoreCriteria(None, CommitsEndpoint.DefaultPageLimit))).thenReturn(SamplePendingCommits)
    get("/") {
      status should be(200)
      body should equal(asJson(SamplePendingCommits))
    }
  }

  "GET /commits?limit=-1" should "return error status with message" in {
    givenStandardAuthenticatedUser()
    // when
    get("/?limit=-1") {
      status should be(400)
      body should equal("limit value must be positive")
    }
  }

  "GET /commits?limit=0" should "return error status with message" in {
    givenStandardAuthenticatedUser()
    // when
    get("/?limit=0") {
      status should be(400)
      body should equal("limit value must be positive")
    }
  }

    "GET /commits?lastId=5203a1383004c520de95ee76" should "query for commits with proper skip value" in {
    val userId = givenStandardAuthenticatedUser()
    val expectedLastId = new ObjectId("5203a1383004c520de95ee76")
    when(commitsListFinder.findCommitsToReviewForUser(userId, LoadMoreCriteria(Some(expectedLastId), CommitsEndpoint.DefaultPageLimit))).thenReturn(SamplePendingCommits)
    get("/?skip=5203a1383004c520de95ee76") {
      status should be(200)
      body should equal(asJson(SamplePendingCommits))
    }
  }

  "GET /commits?lastId=5203a1383004c520de95ee76&limit=2" should "query for commits with proper limit value" in {
    val userId = givenStandardAuthenticatedUser()
    val expectedLastId = new ObjectId("5203a1383004c520de95ee76")
    when(commitsListFinder.findCommitsToReviewForUser(userId, LoadMoreCriteria(Some(expectedLastId), 2))).thenReturn(SamplePendingCommits)
    get("/?lastId=5203a1383004c520de95ee76&limit=2") {
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

  "GET /commits/5203a1383004c520de95ee76/context" should "should return commit with its surrounding" in {
    val userId = givenStandardAuthenticatedUser()
    when(commitsListFinder.findSurroundings(LoadSurroundingsCriteria(new ObjectId("5203a1383004c520de95ee76"), CommitsEndpoint.DefaultSurroundingsCount), userId)).thenReturn(Right(SamplePendingCommits))
    get("/5203a1383004c520de95ee76/context") {
      status should be(200)
      body should equal(asJson(SamplePendingCommits))
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
    extends CommitsServlet(fakeAuthenticator, commitsListFinder, userReactionFinder, commentActivity, commitReviewTaskDao, userReactionService, userDao, new CodebragSwagger, diffService, unlikeUseCaseFactory) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}