package com.softwaremill.codebrag.service.comments

import com.softwaremill.codebrag.dao.CommitCommentDAO
import com.softwaremill.codebrag.domain.{InlineComment, CommentBase, CommitComment}
import com.softwaremill.codebrag.service.comments.command.{NewInlineComment, NewWholeCommitComment, NewComment, AddComment}
import pl.softwaremill.common.util.time.Clock
import org.bson.types.ObjectId

class CommentService(commentDao: CommitCommentDAO)(implicit clock: Clock) {

  @deprecated("Will be removed in favor of method taking NewComment as param")
  def addCommentToCommit(command: AddComment): CommitComment = {
    val temp = NewWholeCommitComment(command.commitId, command.authorId, command.message)
    addCommentToCommit(temp).asInstanceOf[CommitComment]
  }

  def addCommentToCommit(command: NewComment): CommentBase = {
    val commentAsDomainObject = toDomainObject(command)
    commentDao.save(commentAsDomainObject)
    commentAsDomainObject
  }

  private def toDomainObject(newComment: NewComment): CommentBase = {
    newComment match {
      case c: NewWholeCommitComment => CommitComment(new ObjectId, c.commitId, c.authorId, c.message, clock.currentDateTimeUTC())
      case c: NewInlineComment => InlineComment(toDomainObject(c.comment).asInstanceOf[CommitComment], c.fileName, c.lineNumber)
    }
  }
}