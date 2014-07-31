package com.softwaremill.codebrag.rest

import org.scalatra._
import com.softwaremill.codebrag.service.user.Authenticator
import json.JacksonJsonSupport
import swagger.{Swagger, SwaggerSupport}

import com.softwaremill.codebrag.service.diff.DiffWithCommentsService
import com.softwaremill.codebrag.usecases.ReviewCommitUseCase
import com.softwaremill.codebrag.service.comments.UserReactionService
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.finders.reaction.ReactionFinder
import com.softwaremill.codebrag.finders.commits.toreview.ToReviewCommitsFinder
import com.softwaremill.codebrag.finders.commits.all.AllCommitsFinder
import com.softwaremill.codebrag.usecases.reactions.{UnlikeUseCase, LikeUseCase, AddCommentUseCase}

class CommitsServlet(val authenticator: Authenticator,
                     val reviewableCommitsListFinder: ToReviewCommitsFinder,
                     val allCommitsFinder: AllCommitsFinder,
                     val reactionFinder: ReactionFinder,
                     val addCommentUseCase: AddCommentUseCase,
                     val reviewCommitUseCase: ReviewCommitUseCase,
                     val userReactionService: UserReactionService,
                     val userDao: UserDAO, val swagger: Swagger,
                     val diffService: DiffWithCommentsService,
                     val unlikeUseCase: UnlikeUseCase,
                     val likeUseCase: LikeUseCase)
  extends JsonServletWithAuthentication with JacksonJsonSupport with SwaggerSupport with CommitsEndpoint with CommentsEndpoint with LikesEndpoint {

  override protected val applicationName = Some(CommitsServlet.MAPPING_PATH)
  override protected val applicationDescription = "Commits information endpoint"

}

object CommitsServlet {
  val MAPPING_PATH = "commits"
}

