package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.service.comments.{UserReactionService, LikeValidator}
import com.softwaremill.codebrag.service.data.UserJson
import org.bson.types.ObjectId

class UnlikeUseCase(likeValidator: LikeValidator, userReactionService: UserReactionService) {

  def execute(currentUser: UserJson, likeId: ObjectId): Either[String, Unit] = {
    canExecute(currentUser, likeId).right.map(_ => userReactionService.removeLike(likeId))
  }

  private def canExecute(currentUser: UserJson, likeId: ObjectId): Either[String, Unit] = {
    likeValidator.canUserDoUnlike(new ObjectId(currentUser.id), likeId) match {
      case Left(err) => Left(err)
      case _ => Right()
    }
  }

}