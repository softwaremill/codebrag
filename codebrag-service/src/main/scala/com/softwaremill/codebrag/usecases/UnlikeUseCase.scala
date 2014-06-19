package com.softwaremill.codebrag.usecases

import com.softwaremill.codebrag.service.comments.{UserReactionService, LikeValidator}
import com.softwaremill.codebrag.service.data.UserJson
import org.bson.types.ObjectId
import com.softwaremill.codebrag.licence.LicenceService

class UnlikeUseCase(likeValidator: LikeValidator, userReactionService: UserReactionService, licenceService: LicenceService) {

  type UnlikeResult = Either[String, Unit]

  def execute(currentUser: UserJson, likeId: ObjectId): Either[String, Unit] = {
    ifCanExecute(currentUser.idAsObjectId, likeId) {
      userReactionService.removeLike(likeId)
      Right()
    }
  }

  protected def ifCanExecute(userId: ObjectId, likeId: ObjectId)(block: => UnlikeResult): UnlikeResult = {
    licenceService.interruptIfLicenceExpired()
    likeValidator.canUserDoUnlike(userId, likeId) match  {
      case Right(_) => block
      case Left(err) => Left(err)
    }
  }

}