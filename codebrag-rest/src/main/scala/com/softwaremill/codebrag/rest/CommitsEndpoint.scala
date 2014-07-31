package com.softwaremill.codebrag.rest

import org.scalatra.{BadRequest, NotFound}
import com.softwaremill.codebrag.service.diff.DiffWithCommentsService
import org.bson.types.ObjectId
import CommitsEndpoint._
import com.softwaremill.codebrag.common.paging.PagingCriteria
import PagingCriteria.Direction
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.finders.commits.toreview.ToReviewCommitsFinder
import com.softwaremill.codebrag.finders.commits.all.AllCommitsFinder
import com.softwaremill.codebrag.finders.browsingcontext.UserBrowsingContext
import com.softwaremill.codebrag.usecases.reactions.ReviewCommitUseCase

trait CommitsEndpoint extends JsonServletWithAuthentication {

  def diffService: DiffWithCommentsService

  def reviewableCommitsListFinder: ToReviewCommitsFinder
  def allCommitsFinder: AllCommitsFinder

  def reviewCommitUseCase: ReviewCommitUseCase

  before() {
    haltIfNotAuthenticated
  }

  get("/:repo/:sha") {
    val commitSha = params("sha")
    val repoName = params("repo")
    diffService.diffWithCommentsFor(repoName, commitSha, user.id) match {
      case Right(commitWithComments) => commitWithComments
      case Left(error) => NotFound(error)
    }
  }

  delete("/:repo/:sha") {
    reviewCommitUseCase.execute(params("repo"), params("sha"), user.id).left.map { err =>
      BadRequest(Map("err" -> err))
    }
  }

  get("/:repo", allCommits) {
    val paging = extractPagingCriteria
    val context = extractBrowsingContext
    logger.debug(s"Attempting to fetch all commits with possible paging: ${paging}")
    allCommitsFinder.find(context, paging)
  }

  get("/:repo", commitsToReview) {
    val paging = extractPagingCriteria
    val context = extractBrowsingContext
    logger.debug(s"Attempting to fetch commits to review with context $context and paging: $paging")
    reviewableCommitsListFinder.find(context, paging)
  }

  get("/:repo", contextualCommits) {
    val limit = params.getOrElse(LimitParamName, DefaultPageLimit.toString).toInt
    val context = extractBrowsingContext
    val paging = params.get(SelectedShaParamName) match {
      case Some(commitSha) => PagingCriteria(commitSha, Direction.Radial, limit)
      case None => PagingCriteria.fromEnd[String](limit)
    }
    logger.debug(s"Attempting to fetch commits in context: ${paging}")
    allCommitsFinder.find(context, paging)
  }

  private def contextualCommits = params.get(ContextParamName).isDefined
  private def commitsToReview = params.get(FilterParamName).isDefined && params(FilterParamName) == ToReviewCommitsFilter
  private def allCommits = params.get(FilterParamName).isDefined && params(FilterParamName) == AllCommitsFilter

  private def extractBrowsingContext = UserBrowsingContext(user.id, extractReqUrlParam("repo"), extractReqUrlParam("branch"))

  private def extractPagingCriteria = {
    val minSha = params.get(MinShaParamName)
    val maxSha = params.get(MaxShaParamName)
    val limit = params.getOrElse(LimitParamName, CommitsEndpoint.DefaultPageLimit.toString).toInt

    if(maxSha.isDefined) {
      PagingCriteria(maxSha, Direction.Left, limit)
    } else if(minSha.isDefined) {
      PagingCriteria(minSha, Direction.Right, limit)
    } else {
      PagingCriteria.fromBeginning[String](limit)
    }
  }

}

object CommitsEndpoint {

  val DefaultSurroundingsCount = 7
  val DefaultPageLimit = 7

  val ContextParamName = "context"
  val LimitParamName = "limit"
  val BranchParamName = "branch"
  val RepoParamName = "repo"

  val FilterParamName = "filter"
  val AllCommitsFilter = "all"
  val ToReviewCommitsFilter = "to_review"

  val MinShaParamName = "min_sha"
  val MaxShaParamName = "max_sha"
  val SelectedShaParamName = "selected_sha"

}