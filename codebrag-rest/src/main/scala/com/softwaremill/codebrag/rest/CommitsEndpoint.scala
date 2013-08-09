package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.common.{LoadMoreCriteria, LoadSurroundingsCriteria}
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

  before() {
    haltIfNotAuthenticated
  }

  get("/") {
    val filterOpt = params.get("filter")
    val limit = extractPathIntOrHalt("limit", CommitsEndpoint.DefaultPageLimit, "limit value must be positive", (_ > 0))
    filterOpt match {
      case Some("all") => fetchAllCommits()
      case _ => {
        val lastIdOpt = params.get("lastId") match {
          case Some(id) => Some(new ObjectId(id))
          case None => None
        }
        val criteria = LoadMoreCriteria(lastIdOpt, limit)
        commitListFinder.findCommitsToReviewForUser(new ObjectId(user.id), criteria)
      }
    }
  }

  get("/:id/context") {
    val commitId = new ObjectId(params("id"))
    val limit = params.getOrElse("limit", CommitsEndpoint.DefaultSurroundingsCount.toString)
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

  private def fetchAllCommits() = commitListFinder.findAll(new ObjectId(user.id))
}

object CommitsEndpoint {

  val DefaultSurroundingsCount = 7
  val DefaultPageLimit = 7

}