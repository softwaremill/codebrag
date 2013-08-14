package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.common.{PagingCriteria, SurroundingsCriteria}
import org.scalatra.NotFound
import com.softwaremill.codebrag.dao.reporting._
import com.softwaremill.codebrag.service.diff.DiffWithCommentsService
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.CommitReviewTaskDAO
import com.softwaremill.codebrag.domain.CommitReviewTask
import CommitsEndpoint._
import com.softwaremill.codebrag.dao.finders.commit.{ReviewableCommitsListFinder, AllCommitsFinder}

trait CommitsEndpoint extends JsonServletWithAuthentication {

  def diffService: DiffWithCommentsService

  def reviewableCommitsListFinder: ReviewableCommitsListFinder
  def allCommitsFinder: AllCommitsFinder

  def commitReviewTaksDao: CommitReviewTaskDAO

  before() {
    haltIfNotAuthenticated
  }

  get("/:id") {
    val commitId = params("id")
    diffService.diffWithCommentsFor(new ObjectId(commitId), new ObjectId(user.id)) match {
      case Right(commitWithComments) => commitWithComments
      case Left(error) => NotFound(error)
    }
  }

  delete("/:id") {
    val commitId = new ObjectId(params("id"))
    val userId = new ObjectId(user.id)
    val reviewTask = CommitReviewTask(commitId, userId)
    commitReviewTaksDao.delete(reviewTask)
  }

  get("/", allCommits) {
    logger.debug("Attempting to fetch all commits with possible paging")
    val currentUserId = new ObjectId(user.id)
    allCommitsFinder.findAllCommits(extractPagingCriteria, currentUserId)
  }

  get("/", commitsToReview) {
    logger.debug("Attempting to fetch commits to review with possible paging")
    val currentUserId = new ObjectId(user.id)
    reviewableCommitsListFinder.findCommitsToReviewFor(currentUserId, extractPagingCriteria)
  }

  get("/", contextualCommits) {
    logger.debug("Attempting to fetch commits in context")
    val limit = params.getOrElse(LimitParamName, DefaultPageLimit.toString).toInt
    val currentUserId = new ObjectId(user.id)
    params.get("id") match {
      case Some(commitId) => {
        logger.debug(s"Commit id provided $commitId")
        val criteria = SurroundingsCriteria(new ObjectId(commitId), limit)
        allCommitsFinder.findWithSurroundings(criteria, currentUserId)
      }
      case None => {
        logger.debug(s"Commit id not provided fetching first commits")
        val criteria = PagingCriteria(None, None, DefaultSurroundingsCount)
        allCommitsFinder.findAllCommits(criteria, currentUserId)
      }
    }
  }

  private def contextualCommits = params.get(ContextParamName).isDefined
  private def commitsToReview = params.get(FilterParamName).isDefined && params(FilterParamName) == ToReviewCommitsFilter
  private def allCommits = params.get(FilterParamName).isDefined && params(FilterParamName) == AllCommitsFilter

  private def extractPagingCriteria = {
    val minId = params.get(MinIdParamName).map(new ObjectId(_))
    val maxId = params.get(MaxIdParamName).map(new ObjectId(_))
    val limit = params.getOrElse(LimitParamName, CommitsEndpoint.DefaultPageLimit.toString).toInt
    PagingCriteria(maxId, minId, limit)
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

}