package com.softwaremill.codebrag.service.comments

import com.softwaremill.codebrag.dao.CommitCommentDAO
import com.softwaremill.codebrag.domain.{InlineCommitComment, CommentBase, EntireCommitComment}
import com.softwaremill.codebrag.service.comments.command.{NewInlineCommitComment, NewEntireCommitComment, NewComment}
import pl.softwaremill.common.util.time.Clock
import org.bson.types.ObjectId

class CommentService(commentDao: CommitCommentDAO)(implicit clock: Clock) {

  def addCommentToCommit(command: NewComment): CommentBase = {
    val commentAsDomainObject = toDomainObject(command)
    commentDao.save(commentAsDomainObject)
    commentAsDomainObject
  }

  private def toDomainObject(newComment: NewComment): CommentBase = {
    newComment match {
      case c: NewEntireCommitComment => EntireCommitComment(new ObjectId, c.commitId, c.authorId, c.message, clock.currentDateTimeUTC())
      case c: NewInlineCommitComment => InlineCommitComment(toDomainObject(c.comment).asInstanceOf[EntireCommitComment], c.fileName, c.lineNumber)
    }
  }
}