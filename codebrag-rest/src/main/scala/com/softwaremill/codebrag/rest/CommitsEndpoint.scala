package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.common.{LoadSurroundingsCriteria, PagingCriteria}
import org.scalatra.NotFound
import com.softwaremill.codebrag.dao.reporting._
import com.softwaremill.codebrag.service.diff.DiffWithCommentsService
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.CommitReviewTaskDAO
import com.softwaremill.codebrag.domain.CommitReviewTask

trait CommitsEndpoint extends JsonServletWithAuthentication {

  def diffService: DiffWithCommentsService
  def commitListFinder: CommitFinder
  def commitReviewTaksDao: CommitReviewTaskDAO

  val DefaultPaging = PagingCriteria(0, 7)
  val DefaultSurroundingsCount = 7

  before() {
    haltIfNotAuthenticated
  }

  get("/") {
    val filterOpt = params.get("filter")
    val skip = extractPathIntOrHalt("skip", DefaultPaging.skip, "skip value must be non-negative", (_ >= 0))
    val limit = extractPathIntOrHalt("limit", DefaultPaging.limit, "limit value must be positive", (_ > 0))

    filterOpt match {
      case Some("all") => fetchAllCommits()
      case _ => fetchCommitsPendingReview(PagingCriteria(skip, limit))
    }
  }

  get("/:id/context") {
    val commitId = new ObjectId(params("id"))
    val limit = params.getOrElse("limit", DefaultSurroundingsCount.toString)
    commitListFinder.findSurroundings(LoadSurroundingsCriteria(commitId, limit.toInt), new ObjectId(user.id)) match {
      case Right(commits) => commits
      case Left(error) => NotFound(error)
    }
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

  private def fetchCommitsPendingReview(paging: PagingCriteria) = commitListFinder.findCommitsToReviewForUser(new ObjectId(user.id), paging)
  private def fetchAllCommits() = commitListFinder.findAll(new ObjectId(user.id))
}