package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.common.ObjectIdGenerator
import org.scalatra.NotFound
import org.scalatra.swagger.SwaggerSupport
import com.softwaremill.codebrag.dao.reporting._
import com.softwaremill.codebrag.service.diff.DiffWithCommentsService
import com.softwaremill.codebrag.service.github.GitHubCommitImportServiceFactory
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.CommitReviewTaskDAO
import com.softwaremill.codebrag.domain.CommitReviewTask
import com.softwaremill.codebrag.dao.reporting.views.{CommitListView, CommitDetailsWithCommentsView}

trait CommitsEndpoint extends JsonServletWithAuthentication with CommitsEndpointSwaggerDefinition {

  def importerFactory: GitHubCommitImportServiceFactory
  def diffService: DiffWithCommentsService
  def commitListFinder: CommitFinder
  def commitReviewTaksDao: CommitReviewTaskDAO

  before() {
    haltIfNotAuthenticated
  }


  get("/", operation(getCommitsOperation)) {
    fetchCommitsList()
  }

  post("/sync") {
    // synchronizes commits
    implicit val idGenerator = new ObjectIdGenerator()
    val importer = importerFactory.createInstance(user.email)
    importer.importRepoCommits("softwaremill", "codebrag")
    fetchCommitsList()
  }

  get("/:id") {
    val commitId = params("id")
    diffService.diffWithCommentsFor(new ObjectId(commitId)) match {
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

  private def fetchCommitsList() = commitListFinder.findCommitsToReviewForUser(new ObjectId(user.id))
}

trait CommitsEndpointSwaggerDefinition extends SwaggerSupport {

  val getCommitsOperation = apiOperation[CommitListView]("get")
    .summary("Gets all commits to review for current user ")

  val markCommitAsReviewed = apiOperation[Unit]("delete")
    .summary("Removes given commit from user list of commits remaining to review")
    .parameter(pathParam[String]("id").description("Identifier of the commit").required)


  val getFilesForCommit = apiOperation[List[CommitDetailsWithCommentsView]]("get")
    .summary("Get a list of files with diffs")
    .parameter(pathParam[String]("id").description("Identifier of the commit").required)
}