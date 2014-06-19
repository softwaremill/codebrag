package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.service.user.UserJsonBuilder._
import com.softwaremill.codebrag.AuthenticatableServletSpec
import org.scalatra.auth.Scentry
import com.softwaremill.codebrag.service.data.UserJson
import org.mockito.Mockito._
import com.softwaremill.codebrag.service.diff.DiffWithCommentsService
import com.softwaremill.codebrag.usecases.{LikeUseCase, UnlikeUseCase, ReviewCommitUseCase, AddCommentUseCase}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.comments.UserReactionService
import com.softwaremill.codebrag.common.paging.PagingCriteria
import PagingCriteria.Direction
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.finders.reaction.ReactionFinder
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.finders.commits.toreview.ToReviewCommitsFinder
import com.softwaremill.codebrag.finders.commits.all.AllCommitsFinder
import com.softwaremill.codebrag.domain.UserBrowsingContext

class CommitsServletSpec extends AuthenticatableServletSpec {

  import CommitsEndpoint._

  var commentActivity = mock[AddCommentUseCase]
  var commitsInfoDao = mock[CommitInfoDAO]

  var toReviewCommitsFinder = mock[ToReviewCommitsFinder]
  var allCommitsFinder = mock[AllCommitsFinder]

  var diffService = mock[DiffWithCommentsService]
  var userReactionFinder = mock[ReactionFinder]
  var userDao = mock[UserDAO]
  var reviewCommitUseCase = mock[ReviewCommitUseCase]
  val UserJson = someUser
  val userReactionService = mock[UserReactionService]
  val unlikeUseCaseFactory = mock[UnlikeUseCase]
  val likeUseCase = mock[LikeUseCase]

  val repoName = "codebrag"
  val branchName = "master"

  override def beforeEach {
    super.beforeEach
    diffService = mock[DiffWithCommentsService]
    allCommitsFinder = mock[AllCommitsFinder]
    toReviewCommitsFinder = mock[ToReviewCommitsFinder]
    addServlet(new TestableCommitsServlet(fakeAuthenticator, fakeScentry), "/*")
  }

  "GET /:repo/:sha" should "load given commit details" in {
    val userId = givenStandardAuthenticatedUser()
    val commitSha = "12345"

    get(s"/$repoName/$commitSha") {
      verify(diffService).diffWithCommentsFor(repoName, commitSha, userId)
    }
  }

  "DELETE /:repo/:id" should "remove given commit from to review tasks" in {
    val userId = givenStandardAuthenticatedUser()
    val commitSha = "12345"

    delete(s"/$repoName/$commitSha") {
      verify(reviewCommitUseCase).execute(repoName, commitSha, userId)
    }
  }

  "GET /:repo with filter=all" should "load all commits" in {
    val userId = givenStandardAuthenticatedUser()
    val criteria = PagingCriteria.fromBeginning[String](CommitsEndpoint.DefaultPageLimit)

    get(s"/$repoName?$FilterParamName=$AllCommitsFilter") {
      verify(allCommitsFinder).find(userId, Some(repoName), None, criteria)
    }
  }

  "GET /:repo with filter=to_review" should "load commits to review" in {
    val userId = givenStandardAuthenticatedUser()
    val criteria = PagingCriteria.fromBeginning[String](CommitsEndpoint.DefaultPageLimit)
    val context = UserBrowsingContext(userId, repoName, branchName)

    get(s"/$repoName?$BranchParamName=$branchName&$FilterParamName=$ToReviewCommitsFilter") {
      verify(toReviewCommitsFinder).find(context, criteria)
    }
  }

  "GET /:reo with context=true" should "load commits with surroundings" in {
    val userId = givenStandardAuthenticatedUser()
    val commitId = "123456"

    get(s"/$repoName?$ContextParamName=true&$SelectedShaParamName=" + commitId.toString) {
      val criteria = PagingCriteria(commitId, Direction.Radial, CommitsEndpoint.DefaultPageLimit)
      verify(allCommitsFinder).find(userId, Some(repoName), None, criteria)
    }
  }

  "GET /:repo with context=true and no id provided" should "load first commits" in {
    val userId = givenStandardAuthenticatedUser()

    get(s"/$repoName?$ContextParamName=true") {
      val criteria = PagingCriteria.fromEnd[String](CommitsEndpoint.DefaultPageLimit)
      verify(allCommitsFinder).find(userId, Some(repoName), None, criteria)
    }
  }

  "GET /:repo with paging criteria" should "call service with proper criteria object" in {
    val userId = givenStandardAuthenticatedUser()
    val context = UserBrowsingContext(userId, repoName, branchName)
    val lastKnownCommitId = "123456"
    get(s"/$repoName?$BranchParamName=$branchName&$FilterParamName=$ToReviewCommitsFilter&$LimitParamName=10&$MinShaParamName=" + lastKnownCommitId.toString) {
      val criteria = PagingCriteria(lastKnownCommitId, Direction.Right, 10)
      verify(toReviewCommitsFinder).find(context, criteria)
    }
    get(s"/$repoName?$BranchParamName=$branchName&$FilterParamName=$ToReviewCommitsFilter&$LimitParamName=10&$MaxShaParamName=" + lastKnownCommitId.toString) {
      val criteria = PagingCriteria(lastKnownCommitId, Direction.Left, 10)
      verify(toReviewCommitsFinder).find(context, criteria)
    }
  }

  private def givenStandardAuthenticatedUser(): ObjectId = {
    val user = UserJson
    userIsAuthenticatedAs(user)
    new ObjectId(user.id)
  }

  class TestableCommitsServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[UserJson])
    extends CommitsServlet(fakeAuthenticator, toReviewCommitsFinder, allCommitsFinder, userReactionFinder, commentActivity,
      reviewCommitUseCase, userReactionService, userDao, new CodebragSwagger, diffService, unlikeUseCaseFactory, likeUseCase) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}