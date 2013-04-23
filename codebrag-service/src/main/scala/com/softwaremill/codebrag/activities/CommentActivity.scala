package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.service.comments.CommentService
import com.softwaremill.codebrag.service.followups.FollowupService
import com.softwaremill.codebrag.service.comments.command.{NewComment, AddComment}
import com.softwaremill.codebrag.domain.CommitComment

class CommentActivity(commentService: CommentService, followupService: FollowupService) {

  @deprecated("will be removed in next steps in favor of method taking NewComment")
  def commentOnCommit(newComment: AddComment): CommitComment = {
    val addedComment = commentService.addCommentToCommit(newComment)
    followupService.generateFollowupsForComment(newComment)
    addedComment
  }

  // temporary "pass-thru" to help to stay on green with step-by-step refactoring
  def commentOnCommit(newComment: NewComment): CommitComment = {
    val temp = AddComment(newComment.commitId, newComment.authorId, newComment.message)
    commentOnCommit(temp)
  }

}
