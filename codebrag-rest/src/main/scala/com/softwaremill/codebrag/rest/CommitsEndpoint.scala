package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.common.{PagingCriteria, ObjectIdGenerator}
import org.scalatra.NotFound
import org.scalatra.swagger.SwaggerSupport
import com.softwaremill.codebrag.dao.reporting._
import com.softwaremill.codebrag.service.diff.DiffWithCommentsService
import com.softwaremill.codebrag.service.github.GitHubCommitImportServiceFactory
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.CommitReviewTaskDAO
import com.softwaremill.codebrag.domain.CommitReviewTask
import com.softwaremill.codebrag.dao.reporting.views.{CommitDetailsView, CommitListView}

trait CommitsEndpoint extends JsonServletWithAuthentication {

  def importerFactory: GitHubCommitImportServiceFactory
  def diffService: DiffWithCommentsService
  def commitListFinder: CommitFinder
  def commitReviewTaksDao: CommitReviewTaskDAO

  val DefaultPaging = PagingCriteria(0, 7)

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

  post("/sync") {
    // synchronizes commits
    implicit val idGenerator = new ObjectIdGenerator()
    val importer = importerFactory.createInstance(user.login)
    importer.importRepoCommits("softwaremill", "codebrag")
    fetchCommitsPendingReview(DefaultPaging)
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