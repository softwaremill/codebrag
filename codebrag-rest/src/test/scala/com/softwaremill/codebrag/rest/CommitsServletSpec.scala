package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.service.user.UserJsonBuilder._
import com.softwaremill.codebrag.AuthenticatableServletSpec
import org.scalatra.auth.Scentry
import com.softwaremill.codebrag.service.data.UserJson
import org.mockito.Mockito._
import com.softwaremill.codebrag.service.diff.DiffWithCommentsService
import com.softwaremill.codebrag.activities.{CommitReviewActivity, AddCommentActivity}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.CommitReviewTask
import com.softwaremill.codebrag.common.LoadMoreCriteria
import com.softwaremill.codebrag.service.comments.UserReactionService
import com.softwaremill.codebrag.usecase.UnlikeUseCase
import com.softwaremill.codebrag.dao.finders.commit.{AllCommitsFinder, ReviewableCommitsListFinder}
import com.softwaremill.codebrag.common.LoadMoreCriteria.PagingDirection
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.finders.reaction.MongoReactionFinder


class CommitsServletSpec extends AuthenticatableServletSpec {

  var commentActivity = mock[AddCommentActivity]
  var commitsInfoDao = mock[CommitInfoDAO]

  var reviewableCommitsFinder = mock[ReviewableCommitsListFinder]
  var allCommitsFinder = mock[AllCommitsFinder]

  var diffService = mock[DiffWithCommentsService]
  var userReactionFinder = mock[MongoReactionFinder]
  var userDao = mock[UserDAO]
  var commitReviewActivity = mock[CommitReviewActivity]
  val UserJson = someUser()
  val userReactionService = mock[UserReactionService]
  val unlikeUseCaseFactory = mock[UnlikeUseCase]

  override def beforeEach {
    super.beforeEach
    diffService = mock[DiffWithCommentsService]
    allCommitsFinder = mock[AllCommitsFinder]
    reviewableCommitsFinder = mock[ReviewableCommitsListFinder]
    addServlet(new TestableCommitsServlet(fakeAuthenticator, fakeScentry), "/*")
  }

  "GET /:id" should "load given commit details" in {
    val userId = givenStandardAuthenticatedUser()
    val commitId = new ObjectId

    get("/" + commitId.toString) {
      verify(diffService).diffWithCommentsFor(commitId, userId)
    }
  }

  "DELETE /:id" should "remove given commit from to review tasks" in {
    val userId = givenStandardAuthenticatedUser()
    val commitId = new ObjectId
    val reviewTaskToRemove = CommitReviewTask(commitId, userId)

    delete("/" + commitId.toString) {
      verify(commitReviewActivity).markAsReviewed(reviewTaskToRemove)
    }
  }

  "GET / with filter=all" should "load all commits" in {
    val userId = givenStandardAuthenticatedUser()
    val criteria = LoadMoreCriteria.fromBeginning(CommitsEndpoint.DefaultPageLimit)

    get("/?filter=all") {
      verify(allCommitsFinder).findAllCommits(criteria, userId)
    }
  }

  "GET / with filter=to_review" should "load commits to review" in {
    val userId = givenStandardAuthenticatedUser()
    val criteria = LoadMoreCriteria.fromBeginning(CommitsEndpoint.DefaultPageLimit)

    get("/?filter=to_review") {
      verify(reviewableCommitsFinder).findCommitsToReviewFor(userId, criteria)
    }
  }

  "GET / with context=true" should "load commits with surroundings" in {
    val userId = givenStandardAuthenticatedUser()
    val commitId = new ObjectId

    get("/?context=true&id=" + commitId.toString) {
      val criteria = LoadMoreCriteria(commitId, PagingDirection.Radial, CommitsEndpoint.DefaultPageLimit)
      verify(allCommitsFinder).findAllCommits(criteria, userId)
    }
  }

  "GET / with context=true and no id provided" should "load first commits" in {
    val userId = givenStandardAuthenticatedUser()

    get("/?context=true") {
      val criteria = LoadMoreCriteria.fromEnd(CommitsEndpoint.DefaultPageLimit)
      verify(allCommitsFinder).findAllCommits(criteria, userId)
    }
  }

  "GET / with paging criteria" should "call service with proper criteria object" in {
    val userId = givenStandardAuthenticatedUser()
    val lastKnownCommitId = new ObjectId
    get("/?filter=to_review&limit=10&min_id=" + lastKnownCommitId.toString) {
      val criteria = LoadMoreCriteria(lastKnownCommitId, PagingDirection.Right, 10)
      verify(reviewableCommitsFinder).findCommitsToReviewFor(userId, criteria)
    }
    get("/?filter=to_review&limit=10&max_id=" + lastKnownCommitId.toString) {
      val criteria = LoadMoreCriteria(lastKnownCommitId, PagingDirection.Left, 10)
      verify(reviewableCommitsFinder).findCommitsToReviewFor(userId, criteria)
    }
  }

  private def givenStandardAuthenticatedUser(): ObjectId = {
    val user = UserJson
    userIsAuthenticatedAs(user)
    new ObjectId(user.id)
  }

  class TestableCommitsServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[UserJson])
    extends CommitsServlet(fakeAuthenticator, reviewableCommitsFinder, allCommitsFinder, userReactionFinder, commentActivity,
      commitReviewActivity, userReactionService, userDao, new CodebragSwagger, diffService, unlikeUseCaseFactory) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}