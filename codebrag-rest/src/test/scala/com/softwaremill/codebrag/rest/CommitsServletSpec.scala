package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.service.user.UserJsonBuilder._
import com.softwaremill.codebrag.AuthenticatableServletSpec
import org.scalatra.auth.Scentry
import com.softwaremill.codebrag.service.data.UserJson
import org.mockito.Mockito._
import com.softwaremill.codebrag.service.diff.DiffWithCommentsService
import com.softwaremill.codebrag.activities.{UnlikeUseCase, CommitReviewActivity, AddCommentActivity}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.comments.UserReactionService
import com.softwaremill.codebrag.common.paging.PagingCriteria
import PagingCriteria.Direction
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.finders.reaction.ReactionFinder
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.activities.finders.{AllCommitsFinder, ToReviewCommitsFinder}

class CommitsServletSpec extends AuthenticatableServletSpec {

  import CommitsEndpoint._

  var commentActivity = mock[AddCommentActivity]
  var commitsInfoDao = mock[CommitInfoDAO]

  var toReviewCommitsFinder = mock[ToReviewCommitsFinder]
  var allCommitsFinder = mock[AllCommitsFinder]

  var diffService = mock[DiffWithCommentsService]
  var userReactionFinder = mock[ReactionFinder]
  var userDao = mock[UserDAO]
  var commitReviewActivity = mock[CommitReviewActivity]
  val UserJson = someUser()
  val userReactionService = mock[UserReactionService]
  val unlikeUseCaseFactory = mock[UnlikeUseCase]

  override def beforeEach {
    super.beforeEach
    diffService = mock[DiffWithCommentsService]
    allCommitsFinder = mock[AllCommitsFinder]
    toReviewCommitsFinder = mock[ToReviewCommitsFinder]
    addServlet(new TestableCommitsServlet(fakeAuthenticator, fakeScentry), "/*")
  }

  "GET /:id" should "load given commit details" in {
    val userId = givenStandardAuthenticatedUser()
    val commitSha = "12345"

    get("/" + commitSha) {
      verify(diffService).diffWithCommentsFor(commitSha, userId)
    }
  }

  "DELETE /:id" should "remove given commit from to review tasks" in {
    val userId = givenStandardAuthenticatedUser()
    val commitSha = "12345"

    delete("/" + commitSha) {
      verify(commitReviewActivity).markAsReviewed(commitSha, userId)
    }
  }

  "GET / with filter=all" should "load all commits" in {
    val userId = givenStandardAuthenticatedUser()
    val criteria = PagingCriteria.fromBeginning[String](CommitsEndpoint.DefaultPageLimit)

    get(s"/?${FilterParamName}=${AllCommitsFilter}") {
      verify(allCommitsFinder).find(userId, MasterBranchName, criteria)
    }
  }

  "GET / with filter=to_review" should "load commits to review" in {
    val userId = givenStandardAuthenticatedUser()
    val criteria = PagingCriteria.fromBeginning[String](CommitsEndpoint.DefaultPageLimit)

    get(s"/?${FilterParamName}=${ToReviewCommitsFilter}") {
      verify(toReviewCommitsFinder).find(userId, MasterBranchName, criteria)
    }
  }

  "GET / with context=true" should "load commits with surroundings" in {
    val userId = givenStandardAuthenticatedUser()
    val commitId = "123456"

    get(s"/?${ContextParamName}=true&${SelectedShaParamName}=" + commitId.toString) {
      val criteria = PagingCriteria(commitId, Direction.Radial, CommitsEndpoint.DefaultPageLimit)
      verify(allCommitsFinder).find(userId, MasterBranchName, criteria)
    }
  }

  "GET / with context=true and no id provided" should "load first commits" in {
    val userId = givenStandardAuthenticatedUser()

    get(s"/?${ContextParamName}=true") {
      val criteria = PagingCriteria.fromEnd[String](CommitsEndpoint.DefaultPageLimit)
      verify(allCommitsFinder).find(userId, MasterBranchName, criteria)
    }
  }

  "GET / with paging criteria" should "call service with proper criteria object" in {
    val userId = givenStandardAuthenticatedUser()
    val lastKnownCommitId = "123456"
    get(s"/?${FilterParamName}=${ToReviewCommitsFilter}&${LimitParamName}=10&${MinShaParamName}=" + lastKnownCommitId.toString) {
      val criteria = PagingCriteria(lastKnownCommitId, Direction.Right, 10)
      verify(toReviewCommitsFinder).find(userId, MasterBranchName, criteria)
    }
    get(s"/?${FilterParamName}=${ToReviewCommitsFilter}&${LimitParamName}=10&${MaxShaParamName}=" + lastKnownCommitId.toString) {
      val criteria = PagingCriteria(lastKnownCommitId, Direction.Left, 10)
      verify(toReviewCommitsFinder).find(userId, MasterBranchName, criteria)
    }
  }

  private def givenStandardAuthenticatedUser(): ObjectId = {
    val user = UserJson
    userIsAuthenticatedAs(user)
    new ObjectId(user.id)
  }

  class TestableCommitsServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[UserJson])
    extends CommitsServlet(fakeAuthenticator, toReviewCommitsFinder, allCommitsFinder, userReactionFinder, commentActivity,
      commitReviewActivity, userReactionService, userDao, new CodebragSwagger, diffService, unlikeUseCaseFactory) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}