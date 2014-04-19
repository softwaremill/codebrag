package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.service.comments.UserReactionService
import com.softwaremill.codebrag.service.followups.FollowupService
import com.softwaremill.codebrag.service.comments.command.IncomingComment
import com.softwaremill.codebrag.common.{Clock, EventBus}
import com.softwaremill.codebrag.domain.reactions.CommentAddedEvent

class AddCommentUseCase(
  userReactionService: UserReactionService,
  followupService: FollowupService,
  eventBus: EventBus)
  (implicit clock: Clock) {

  def addCommentToCommit(newComment: IncomingComment) = {
    val addedComment = userReactionService.storeComment(newComment)
    followupService.generateFollowupsForComment(addedComment)
    eventBus.publish(CommentAddedEvent(addedComment))
    addedComment
  }

}
