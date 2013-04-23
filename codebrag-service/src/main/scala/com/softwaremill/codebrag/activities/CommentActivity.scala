package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.service.comments.CommentService
import com.softwaremill.codebrag.service.followups.FollowupService
import com.softwaremill.codebrag.service.comments.command.{NewWholeCommitComment, NewComment, AddComment}
import com.softwaremill.codebrag.domain.CommitComment

class CommentActivity(commentService: CommentService, followupService: FollowupService) {

  @deprecated("will be removed in next steps in favor of method taking NewComment")
  def commentOnCommit(newComment: AddComment): CommitComment = {
    val temp = NewWholeCommitComment(newComment.commitId, newComment.authorId, newComment.message)
    commentOnCommit(temp).asInstanceOf[CommitComment]
  }

  def commentOnCommit(newComment: NewComment) = {
    val addedComment = commentService.addCommentToCommit(newComment)
    followupService.generateFollowupsForComment(addedComment)
    addedComment
  }

}
