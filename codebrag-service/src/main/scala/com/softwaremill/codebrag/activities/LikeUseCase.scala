package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.service.comments.UserReactionService
import com.softwaremill.codebrag.service.comments.command.IncomingLike
import com.softwaremill.codebrag.domain.Like
import com.softwaremill.codebrag.licence.LicenceService

class LikeUseCase(userReactionService: UserReactionService, licenceService: LicenceService) {

  type LikeResult = Either[String, Like]

  def execute(implicit like: IncomingLike): LikeResult = {
    ifCanExecute {
      userReactionService.storeLike(like)
    }
  }

  protected def ifCanExecute(actionBlock: => LikeResult)(implicit like: IncomingLike): LikeResult = {
    licenceService.interruptIfLicenceExpired()
    actionBlock
  }
}