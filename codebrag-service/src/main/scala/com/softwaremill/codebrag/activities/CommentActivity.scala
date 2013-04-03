package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.service.comments.CommentService
import com.softwaremill.codebrag.service.followups.FollowUpService
import com.softwaremill.codebrag.service.comments.command.AddComment

class CommentActivity(commentService: CommentService, followUpService: FollowUpService) {

  def commentOnCommit(newComment: AddComment) = {
    val addedComment = commentService.addCommentToCommit(newComment)
    followUpService.generateFollowUpsForCommit(newComment.commitId)
    addedComment
  }

}
