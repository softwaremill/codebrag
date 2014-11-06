package com.softwaremill.codebrag.usecases.reactions

import com.softwaremill.codebrag.service.comments.UserReactionService
import com.softwaremill.codebrag.service.comments.command.IncomingLike
import com.softwaremill.codebrag.domain.Like

class LikeUseCase(userReactionService: UserReactionService) {

  type LikeResult = Either[String, Like]

  def execute(implicit like: IncomingLike): LikeResult = {
    userReactionService.storeLike(like)
  }
}