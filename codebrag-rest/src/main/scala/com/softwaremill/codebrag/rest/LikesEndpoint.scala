package com.softwaremill.codebrag.rest

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.service.comments.command.IncomingLike
import scala.Some
import com.softwaremill.codebrag.service.comments.UserReactionService
import java.util.Date
import com.softwaremill.codebrag.dao.reporting.{ReactionFinder, MongoReactionFinder}
import com.softwaremill.codebrag.domain.Like
import com.softwaremill.codebrag.dao.reporting.views.LikeView

trait LikesEndpoint extends JsonServletWithAuthentication with UserReactionParametersReader {

  def userReactionService: UserReactionService
  def reactionFinder: ReactionFinder

  post("/:id/likes") {
    haltIfNotAuthenticated()
    val incomingLike = buildIncomingLike
    userReactionService.storeLike(incomingLike) match {
      case Right(savedLike) => response(savedLike)
      case Left(errMessage) => halt(400, errMessage)
    }
  }

  def response(savedLike: Like): LikeView = {
    reactionFinder.findLikeById(savedLike.id) match {
      case Some(likeView) => likeView
      case None => halt(400, s"Invalid user id $savedLike.authorId")
    }
  }

  private def buildIncomingLike = {
    val params = readReactionParamsFromRequest
    IncomingLike(new ObjectId(params.commitId), new ObjectId(user.id), params.fileName, params.lineNumber)
  }

}