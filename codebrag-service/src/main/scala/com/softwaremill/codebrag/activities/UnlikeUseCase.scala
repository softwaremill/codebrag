package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.service.comments.{UserReactionService, LikeValidator}
import com.softwaremill.codebrag.service.data.UserJson
import org.bson.types.ObjectId

class UnlikeUseCase(likeValidator: LikeValidator, userReactionService: UserReactionService) {

  type UnlikeResult = Either[String, Unit]

  def execute(currentUser: UserJson, likeId: ObjectId): Either[String, Unit] = {
    ifCanExecute(currentUser.userId, likeId) {
      userReactionService.removeLike(likeId)
      Right()
    }
  }

  protected def ifCanExecute(userId: ObjectId, likeId: ObjectId)(block: => UnlikeResult): UnlikeResult = {
    likeValidator.canUserDoUnlike(userId, likeId) match  {
      case Right(_) => block
      case Left(err) => Left(err)
    }
  }

}