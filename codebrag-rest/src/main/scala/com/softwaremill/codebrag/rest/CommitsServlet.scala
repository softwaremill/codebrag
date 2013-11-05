package com.softwaremill.codebrag.rest

import org.scalatra._
import com.softwaremill.codebrag.service.user.Authenticator
import json.JacksonJsonSupport
import swagger.{Swagger, SwaggerSupport}

import com.softwaremill.codebrag.dao.reporting._
import com.softwaremill.codebrag.service.diff.DiffWithCommentsService
import com.softwaremill.codebrag.activities.AddCommentActivity
import com.softwaremill.codebrag.dao.{CommitReviewTaskDAO, UserDAO}
import com.softwaremill.codebrag.service.comments.UserReactionService
import com.softwaremill.codebrag.usecase.UnlikeUseCase
import com.softwaremill.codebrag.dao.finders.commit.{ReviewableCommitsListFinder, AllCommitsFinder}

class CommitsServlet(val authenticator: Authenticator,
                     val reviewableCommitsListFinder: ReviewableCommitsListFinder,
                     val allCommitsFinder: AllCommitsFinder,
                     val reactionFinder: ReactionFinder,
                     val commentActivity: AddCommentActivity,
                     val commitReviewTaksDao: CommitReviewTaskDAO,
                     val userReactionService: UserReactionService,
                     val userDao: UserDAO, val swagger: Swagger,
                     val diffService: DiffWithCommentsService,
                     val unlikeUseCase: UnlikeUseCase)
  extends JsonServletWithAuthentication with JacksonJsonSupport with SwaggerSupport with CommitsEndpoint with CommentsEndpoint with LikesEndpoint {

  override protected val applicationName = Some(CommitsServlet.MAPPING_PATH)
  override protected val applicationDescription = "Commits information endpoint"

}

object CommitsServlet {
  val MAPPING_PATH = "commits"
}

