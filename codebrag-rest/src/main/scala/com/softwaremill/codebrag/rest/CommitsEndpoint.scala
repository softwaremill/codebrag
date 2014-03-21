package com.softwaremill.codebrag.rest

import org.scalatra.NotFound
import com.softwaremill.codebrag.service.diff.DiffWithCommentsService
import org.bson.types.ObjectId
import CommitsEndpoint._
import com.softwaremill.codebrag.common.paging.PagingCriteria
import PagingCriteria.Direction
import com.softwaremill.codebrag.activities.CommitReviewActivity
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.activities.finders.{AllCommitsFinder, ToReviewCommitsFinder}

trait CommitsEndpoint extends JsonServletWithAuthentication {

  def diffService: DiffWithCommentsService

  def reviewableCommitsListFinder: ToReviewCommitsFinder
  def allCommitsFinder: AllCommitsFinder

  def commitReviewActivity: CommitReviewActivity

  before() {
    haltIfNotAuthenticated
  }

  get("/:sha") {
    val commitSha = params("sha")
    diffService.diffWithCommentsFor(commitSha, userId) match {
      case Right(commitWithComments) => commitWithComments
      case Left(error) => NotFound(error)
    }
  }

  delete("/:sha") {
    commitReviewActivity.markAsReviewed(params("sha"), userId)
  }

  get("/", allCommits) {
    val paging = extractPagingCriteria
    logger.debug(s"Attempting to fetch all commits with possible paging: ${paging}")
    allCommitsFinder.find(userId, TemporaryBranchUsed, paging)
  }

  get("/", commitsToReview) {
    val paging = extractPagingCriteria
    logger.debug(s"Attempting to fetch commits to review with possible paging: ${paging}")
    reviewableCommitsListFinder.find(userId, TemporaryBranchUsed, paging)
  }

  get("/", contextualCommits) {
    val limit = params.getOrElse(LimitParamName, DefaultPageLimit.toString).toInt
    val paging = params.get("selected_sha") match {
      case Some(commitSha) => PagingCriteria(commitSha, Direction.Radial, limit)
      case None => PagingCriteria.fromEnd[String](limit)
    }
    logger.debug(s"Attempting to fetch commits in context: ${paging}")
    allCommitsFinder.find(userId, TemporaryBranchUsed, paging)
  }

  private def userId = new ObjectId(user.id)

  private def contextualCommits = params.get(ContextParamName).isDefined
  private def commitsToReview = params.get(FilterParamName).isDefined && params(FilterParamName) == ToReviewCommitsFilter
  private def allCommits = params.get(FilterParamName).isDefined && params(FilterParamName) == AllCommitsFilter

  private def extractPagingCriteria = {
    val minId = params.get(MinIdParamName)
    val maxId = params.get(MaxIdParamName)
    val limit = params.getOrElse(LimitParamName, CommitsEndpoint.DefaultPageLimit.toString).toInt

    if(maxId.isDefined) {
      PagingCriteria(maxId, Direction.Left, limit)
    } else if(minId.isDefined) {
      PagingCriteria(minId, Direction.Right, limit)
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

  val FilterParamName = "filter"
  val AllCommitsFilter = "all"
  val ToReviewCommitsFilter = "to_review"

  val MinIdParamName = "min_id"
  val MaxIdParamName = "max_id"

  val TemporaryBranchUsed = "refs/remotes/origin/master"

}