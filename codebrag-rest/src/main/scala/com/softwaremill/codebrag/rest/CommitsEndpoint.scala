package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.common.ObjectIdGenerator
import org.scalatra.NotFound
import org.scalatra.swagger.SwaggerSupport
import com.softwaremill.codebrag.dao.reporting.{CommitListFinder, CommitListDTO}
import com.softwaremill.codebrag.service.diff.{DiffService, FileWithDiff}
import com.softwaremill.codebrag.service.github.GitHubCommitImportServiceFactory
import org.bson.types.ObjectId

trait CommitsEndpoint extends JsonServletWithAuthentication with CommitsEndpointSwaggerDefinition {

  def importerFactory: GitHubCommitImportServiceFactory
  def diffService: DiffService
  def commitListFinder: CommitListFinder

  get("/", operation(getCommitsOperation)) {
    haltIfNotAuthenticated
    fetchCommitsList()
  }

  post("/sync") {
    // synchronizes commits
    haltIfNotAuthenticated
    implicit val idGenerator = new ObjectIdGenerator()
    val importer = importerFactory.createInstance(user.email)
    importer.importRepoCommits("softwaremill", "codebrag")
    fetchCommitsList()
  }

  get("/:id", operation(getFilesForCommit)) {
    val commitId = params("id")
    diffService.getFilesWithDiffs(commitId) match {
      case Right(files) => files
      case Left(error) => NotFound(error)
    }
  }

  private def fetchCommitsList() = commitListFinder.findCommitsToReviewForUser(new ObjectId(user.id))
}

trait CommitsEndpointSwaggerDefinition extends SwaggerSupport {

  val getCommitsOperation = apiOperation[CommitListDTO]("get")
    .summary("Gets all commits to review for current user ")

  val getFilesForCommit = apiOperation[List[FileWithDiff]]("get")
    .summary("Get a list of files with diffs")
    .parameter(pathParam[String]("id").description("Identifier of the commit").required)
}

