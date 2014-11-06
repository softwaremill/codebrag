package com.softwaremill.codebrag.usecases.reactions

import com.softwaremill.codebrag.service.comments.UserReactionService
import com.softwaremill.codebrag.service.followups.FollowupService
import com.softwaremill.codebrag.service.comments.command.IncomingComment
import com.softwaremill.codebrag.common.{Clock, EventBus}
import com.softwaremill.codebrag.domain.reactions.CommentAddedEvent
import com.softwaremill.codebrag.domain.Comment

class AddCommentUseCase(userReactionService: UserReactionService, followupService: FollowupService, eventBus: EventBus)(implicit clock: Clock) {

  type AddCommentResult = Either[String, Comment]

  def execute(implicit newComment: IncomingComment): AddCommentResult = {
    val addedComment = userReactionService.storeComment(newComment)
    followupService.generateFollowupsForComment(addedComment)
    eventBus.publish(CommentAddedEvent(addedComment))
    Right(addedComment)
  }

}
