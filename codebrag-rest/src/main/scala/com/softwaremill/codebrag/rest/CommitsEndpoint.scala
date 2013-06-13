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
import com.softwaremill.codebrag.dao.reporting.views.{CommitDetailsWithCommentsView, CommitListView}

trait CommitsEndpoint extends JsonServletWithAuthentication with CommitsEndpointSwaggerDefinition {

  def importerFactory: GitHubCommitImportServiceFactory
  def diffService: DiffWithCommentsService
  def commitListFinder: CommitFinder
  def commitReviewTaksDao: CommitReviewTaskDAO

  before() {
    haltIfNotAuthenticated
  }

  get("/", operation(getCommitsOperation)) {
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

  get("/:id", operation(getDetailsOperation)) {
    val commitId = params("id")
    diffService.diffWithCommentsFor(new ObjectId(commitId), new ObjectId(user.id)) match {
      case Right(commitWithComments) => commitWithComments
      case Left(error) => NotFound(error)
    }
  }

  delete("/:id", operation(markCommitAsReviewed)) {
    val commitId = new ObjectId(params("id"))
    val userId = new ObjectId(user.id)
    val reviewTask = CommitReviewTask(commitId, userId)
    commitReviewTaksDao.delete(reviewTask)
  }

  private def fetchCommitsPendingReview(paging: PagingCriteria) = commitListFinder.findCommitsToReviewForUser(new ObjectId(user.id), paging)
  private def fetchAllCommits() = commitListFinder.findAll(new ObjectId(user.id))
}

trait CommitsEndpointSwaggerDefinition extends SwaggerSupport {
  val DefaultPaging = PagingCriteria(0, 7)

  val getCommitsOperation = apiOperation[CommitListView]("get")
    .summary("Gets all commits to review for current user ")
    .parameter(queryParam[String]("filter").description("What kind of commits should be fetched")
    .allowableValues("all", "pending")
    .defaultValue("pending").optional)
    .parameter(queryParam[Int]("skip").description("Numbers of elements to skip (for pending filter mode)")
    .defaultValue(DefaultPaging.skip).optional)
    .parameter(queryParam[Int]("limit").description("Maximum number of elements to return (for pending filter mode)")
    .defaultValue(DefaultPaging.limit).optional)

  val getDetailsOperation = apiOperation[CommitDetailsWithCommentsView]("details")
    .summary("Gets commit details with diff, comments, likes, etc.")
    .parameter(pathParam[String]("id").description("Commit identifier"))


  val markCommitAsReviewed = apiOperation[Unit]("delete")
    .summary("Removes given commit from user list of commits remaining to review")
    .parameter(pathParam[String]("id").description("Identifier of the commit").required)
}