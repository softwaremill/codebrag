package com.softwaremill.codebrag.rest

import org.scalatra._
import com.softwaremill.codebrag.service.user.Authenticator
import json.JacksonJsonSupport
import swagger.{Swagger, SwaggerSupport}
import com.softwaremill.codebrag.dao.CommitInfoDAO
import com.softwaremill.codebrag.service.github.{GitHubCommitInfoConverter, GitHubCommitImportService}
import org.eclipse.egit.github.core.service.CommitService

import com.softwaremill.codebrag.service.comments.CommentService
import com.softwaremill.codebrag.dao.reporting._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.ObjectIdGenerator
import com.softwaremill.codebrag.service.diff.DiffService
import com.softwaremill.codebrag.dao.reporting.CommitListDTO
import scala.Some
import com.softwaremill.codebrag.dao.reporting.CommentListDTO
import com.softwaremill.codebrag.service.diff.FileWithDiff
import com.softwaremill.codebrag.service.comments.command.AddComment

class CommitsServlet(val authenticator: Authenticator, commitInfoDao: CommitInfoDAO,
                     commitListFinder: CommitListFinder,
                     commentListFinder: CommentListFinder,
                     commentService: CommentService, val swagger: Swagger,
                     diffService: DiffService)
  extends JsonServletWithAuthentication with CommitsServletSwaggerDefinition with JacksonJsonSupport {

  get("/") {
    // for all /commits/*
    halt(404)
  }

  post("/:id/comments", operation(addCommentOperation)) {
    haltIfNotAuthenticated
    val commitId = params("id")
    val messageBody = (parsedBody \ "body").extract[String]
    val command = AddComment(new ObjectId(commitId), user.login, messageBody)
    AddCommentResponse(commentService.addCommentToCommit(command))
  }

  get("/:id/comments", operation(getCommentsOperation)) {
    haltIfNotAuthenticated
    val commitId = params("id")
    commentListFinder.findAllForCommit(new ObjectId(commitId))
  }

  get("/", operation(getCommitsOperation)) {
    // for /commits?type=* only
    haltIfNotAuthenticated
    params.get("type") match {
      case Some("pending") => fetchPendingCommits()
      case _ => pass()
    }
  }

  post("/sync") {
    // synchronizes commits
    haltIfNotAuthenticated
    implicit val idGenerator = new ObjectIdGenerator()
    val importer = new GitHubCommitImportService(new CommitService, new GitHubCommitInfoConverter(), commitInfoDao)
    importer.importRepoCommits("pbuda", "testrepo")
    fetchPendingCommits()
  }

  get("/:id", operation(getFilesForCommit)) {
    val commitId = params("id")
    diffService.getFilesWithDiffs(commitId) match {
      case Right(files) => files
      case Left(error) => NotFound(error)
    }
  }

  private def fetchPendingCommits() = commitListFinder.findAllPendingCommits()
}

object CommitsServlet {
  val MAPPING_PATH = "commits"
}

trait CommitsServletSwaggerDefinition extends SwaggerSupport {

  override protected val applicationName = Some(CommitsServlet.MAPPING_PATH)
  protected val applicationDescription: String = "Commits information endpoint"

  val getCommitsOperation = apiOperation[CommitListDTO]("get")
    .parameter(queryParam[String]("type").description("Type of selection (can be 'pending')").required)
    .summary("Gets all commits pending for review")

  val addCommentOperation = apiOperation[AddCommentResponse]("add")
    .summary("Posts a new comment")
    .parameter(pathParam[String]("id").description("Commit identifier").required)
    .parameter(bodyParam[String]("body").description("Message body").required)

  val getCommentsOperation = apiOperation[CommentListDTO]("getList")
    .summary("Get a lists of comments")
    .parameter(pathParam[String]("id").description("Commit identifier").required)

  val getFilesForCommit = apiOperation[List[FileWithDiff]]("get")
    .summary("Get a list of files with diffs")
    .parameter(pathParam[String]("id").description("Identifier of the commit").required)
}

case class AddCommentResponse(item: CommentListItemDTO)