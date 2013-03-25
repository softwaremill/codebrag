package com.softwaremill.codebrag.rest

import org.scalatra._
import com.softwaremill.codebrag.service.user.Authenticator
import json.JacksonJsonSupport
import swagger.{Swagger, SwaggerSupport}
import com.softwaremill.codebrag.dao.CommitInfoDAO
import com.softwaremill.codebrag.domain.CommitInfo
import com.softwaremill.codebrag.service.github.{GitHubCommitInfoConverter, GitHubCommitImportService}
import org.eclipse.egit.github.core.service.CommitService

class CommitsServlet(val authenticator: Authenticator, val commitInfoDao: CommitInfoDAO, val swagger: Swagger)
  extends JsonServletWithAuthentication with CommitsServletSwaggerDefinition with JacksonJsonSupport {

  get("/") { // for all /commits/*
    halt(404)
  }

  post("/:id/comments/", operation(addCommentOperation)) {
    haltIfNotAuthenticated
    val addCommand = parse(request.body).extract[AddCommentWebRequest]
    val commitId = params("id")
    AddCommentWebResponse("new_comment_id")
  }

  get("/") { // for /commits?type=* only
    haltIfNotAuthenticated
    params.get("type") match {
      case Some("pending") => fetchPendingCommits()
      case _ => pass()
    }
  }

  post("/sync") { // synchronizes commits
    haltIfNotAuthenticated
    val importer = new GitHubCommitImportService(new CommitService, new GitHubCommitInfoConverter(), commitInfoDao)
    importer.importRepoCommits("pbuda", "testrepo")
    fetchPendingCommits()
  }

  def fetchPendingCommits(): CommitsResponse = {
    CommitsResponse(commitInfoDao.findAllPendingCommits())
  }
}

object CommitsServlet {
  val MAPPING_PATH = "commits"
}

trait CommitsServletSwaggerDefinition extends SwaggerSupport {

  override protected val applicationName = Some(CommitsServlet.MAPPING_PATH)
  protected val applicationDescription: String = "Commits information endpoint"

  val addCommentOperation = apiOperation[AddCommentWebResponse]("add")
    .summary("Posts a new comment")
    .parameter(pathParam[String]("id").description("Commit identifier").required)
    .parameter(bodyParam[String]("body").description("Message body").required)
}

case class CommitsResponse(commits: Seq[CommitInfo])
case class AddCommentWebRequest(body: String)
case class AddCommentWebResponse(id: String)

