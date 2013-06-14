package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.service.comments.CommentService
import com.softwaremill.codebrag.service.followups.FollowupService
import com.softwaremill.codebrag.service.comments.command.IncomingComment

class AddCommentActivity(commentService: CommentService, followupService: FollowupService) {

  def addCommentToCommit(newComment: IncomingComment) = {
    val addedComment = commentService.addCommentToCommit(newComment)
    followupService.generateFollowupsForComment(addedComment)
    addedComment
  }

}
