package com.softwaremill.codebrag.rest

import org.scalatra._
import com.softwaremill.codebrag.service.user.Authenticator
import json.JacksonJsonSupport
import swagger.{Swagger, SwaggerSupport}

import com.softwaremill.codebrag.dao.reporting._
import com.softwaremill.codebrag.service.diff.DiffService
import scala.Some
import com.softwaremill.codebrag.service.github.GitHubCommitImportServiceFactory
import com.softwaremill.codebrag.activities.CommentActivity
import com.softwaremill.codebrag.dao.{CommitReviewTaskDAO, UserDAO}

class CommitsServlet(val authenticator: Authenticator,
                     val commitListFinder: CommitListFinder,
                     val commentListFinder: CommentListFinder,
                     val commentActivity: CommentActivity,
                     val commitReviewTaksDao: CommitReviewTaskDAO,
                     val userDao: UserDAO, val swagger: Swagger,
                     val diffService: DiffService, val importerFactory: GitHubCommitImportServiceFactory)
  extends JsonServletWithAuthentication with JacksonJsonSupport with SwaggerSupport with CommitsEndpoint with CommentsEndpoint {

  override protected val applicationName = Some(CommitsServlet.MAPPING_PATH)
  override protected val applicationDescription = "Commits information endpoint"

}

object CommitsServlet {
  val MAPPING_PATH = "commits"
}

