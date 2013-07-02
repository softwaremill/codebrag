package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.service.comments.UserReactionService
import com.softwaremill.codebrag.service.followups.FollowupService
import com.softwaremill.codebrag.service.comments.command.IncomingComment
import com.softwaremill.codebrag.domain.Comment

class AddCommentActivity(userReactionService: UserReactionService, followupService: FollowupService) {

  def addCommentToCommit(newComment: IncomingComment) = {
    val addedComment = userReactionService.storeComment(newComment).asInstanceOf[Comment]
    followupService.generateFollowupsForComment(addedComment)
    addedComment
  }

}
