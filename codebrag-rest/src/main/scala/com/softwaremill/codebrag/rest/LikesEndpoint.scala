package com.softwaremill.codebrag.rest

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.service.comments.command.IncomingLike
import scala.Some
import com.softwaremill.codebrag.service.comments.UserReactionService
import java.util.Date

trait LikesEndpoint extends JsonServletWithAuthentication with UserReactionParametersReader {

  def userReactionService: UserReactionService
  def userDao: UserDAO

  post("/:id/likes") {
    haltIfNotAuthenticated()
    val incomingLike = buildIncomingLike
    userReactionService.storeUserReaction(incomingLike) match {
      case Right(savedLike) => {
        userDao.findById(savedLike.authorId) match {
          case Some(user) => LikeResponse(savedLike.id.toString, user.name, savedLike.postingTime.toDate, user.avatarUrl)
          case None => halt(400, s"Invalid user id $savedLike.authorId")
        }
      }
      case Left(errMessage) => halt(400, errMessage)
    }
  }

  private def buildIncomingLike = {
    val params = readReactionParamsFromRequest
    IncomingLike(new ObjectId(params.commitId), new ObjectId(user.id), params.fileName, params.lineNumber)
  }

}

case class LikeResponse(id: String, authorName: String, time: Date, authorAvatarUrl: String = "")
