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
    val like = userReactionService.storeUserReaction(buildIncomingLike)
    userDao.findById(like.authorId) match {
      case Some(user) => LikeView(like.id.toString, user.name, like.postingTime.toDate, user.avatarUrl)
      case None => halt(400, s"Invalid user id $like.authorId")
    }
  }

  private def buildIncomingLike = {
    val params = readReactionParamsFromRequest
    IncomingLike(new ObjectId(params.commitId), new ObjectId(user.id), params.fileName, params.lineNumber)
  }

  case class LikeView(id: String, authorName: String, time: Date, authorAvatarUrl: String = "")

}