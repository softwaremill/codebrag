package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.service.comments.CommentService
import com.softwaremill.codebrag.service.followups.FollowupService
import com.softwaremill.codebrag.service.comments.command.NewComment

class CommentActivity(commentService: CommentService, followupService: FollowupService) {

  def putCommentOnCommit(newComment: NewComment) = {
    val addedComment = commentService.addCommentToCommit(newComment)
    followupService.generateFollowupsForComment(addedComment)
    addedComment
  }

}
