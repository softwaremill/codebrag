package com.softwaremill.codebrag.rest

import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.comments.command.IncomingLike
import scala.Some
import com.softwaremill.codebrag.service.comments.UserReactionService
import com.softwaremill.codebrag.domain.Like
import com.softwaremill.codebrag.dao.finders.reaction.ReactionFinder
import com.softwaremill.codebrag.dao.finders.views.LikeView
import com.softwaremill.codebrag.usecases.{LikeUseCase, UnlikeUseCase}

trait LikesEndpoint extends JsonServletWithAuthentication with UserReactionParametersReader {

  def userReactionService: UserReactionService
  def reactionFinder: ReactionFinder
  def unlikeUseCase: UnlikeUseCase
  def likeUseCase: LikeUseCase

  post("/:id/likes") {
    haltIfNotAuthenticated()
    likeUseCase.execute(incomingLike) match {
      case Right(savedLike) => response(savedLike)
      case Left(errMessage) => halt(400, errMessage)
    }
  }

  delete("/:id/likes/:likeId") {
    haltIfNotAuthenticated()
    val likeId = params.getOrElse("likeId", halt(400, "Missing id of like to remove"))
    unlikeUseCase.execute(user, new ObjectId(likeId)).left.map { err =>
      halt(400, Map("err" -> err, "likeId" -> likeId))
    }
  }

  def response(savedLike: Like): LikeView = {
    reactionFinder.findLikeById(savedLike.id) match {
      case Some(likeView) => likeView
      case None => halt(400, s"Invalid user id $savedLike.authorId")
    }
  }

  private def incomingLike = {
    val params = readReactionParamsFromRequest
    IncomingLike(new ObjectId(params.commitId), user.id, params.fileName, params.lineNumber)
  }

}