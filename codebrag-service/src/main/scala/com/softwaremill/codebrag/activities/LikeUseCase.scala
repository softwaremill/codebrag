package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.service.comments.UserReactionService
import com.softwaremill.codebrag.service.comments.command.IncomingLike
import com.softwaremill.codebrag.domain.Like

class LikeUseCase(userReactionService: UserReactionService) {

  type LikeResult = Either[String, Like]

  def execute(implicit like: IncomingLike): LikeResult = {
    ifCanExecute {
      userReactionService.storeLike(like)
    }
  }

  protected def ifCanExecute(block: => LikeResult)(implicit like: IncomingLike): LikeResult = {
    block
  }
}