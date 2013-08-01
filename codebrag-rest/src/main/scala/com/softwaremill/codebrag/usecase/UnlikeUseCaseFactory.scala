package com.softwaremill.codebrag.usecase

import com.softwaremill.codebrag.service.comments.{UserReactionService, LikeValidator}
import com.softwaremill.codebrag.service.data.UserJson
import org.bson.types.ObjectId

class UnlikeUseCaseFactory(likeValidator: LikeValidator, userReactionService: UserReactionService) {

  def createNew(currentUser: UserJson, likeId: ObjectId) = {
    new UnlikeUseCase(currentUser, likeId, likeValidator, userReactionService)
  }

  class UnlikeUseCase(currentUser: UserJson, likeId: ObjectId, likeValidator: LikeValidator, userReactionService: UserReactionService) extends UseCase {

    def canExecute = {
      likeValidator.canUserDoUnlike(new ObjectId(currentUser.id), likeId) match {
        case Left(err) => Left(err)
        case _ => Right(true)
      }
    }

    def execute() = {
      canExecute.right.map(_ => {
        userReactionService.removeLike(likeId)
      })
    }

  }

}