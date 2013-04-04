package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.service.comments.CommentService
import com.softwaremill.codebrag.service.followups.FollowupService
import com.softwaremill.codebrag.service.comments.command.AddComment

class CommentActivity(commentService: CommentService, followupService: FollowupService) {

  def commentOnCommit(newComment: AddComment) = {
    val addedComment = commentService.addCommentToCommit(newComment)
    followupService.generateFollowupsForCommit(newComment.commitId)
    addedComment
  }

}
