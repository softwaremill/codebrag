package com.softwaremill.codebrag.service.comments

import com.softwaremill.codebrag.dao.CommitCommentDAO
import com.softwaremill.codebrag.domain.Comment
import pl.softwaremill.common.util.time.Clock
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.comments.command.IncomingComment

class CommentService(commentDao: CommitCommentDAO)(implicit clock: Clock) {

  def addCommentToCommit(command: IncomingComment): Comment = {
    val commentAsDomainObject = toDomainObject(command)
    commentDao.save(commentAsDomainObject)
    commentAsDomainObject
  }

  private def toDomainObject(newComment: IncomingComment): Comment = {
    Comment(new ObjectId, newComment.commitId, newComment.authorId, clock.currentDateTimeUTC(), newComment.message, newComment.fileName, newComment.lineNumber)
  }
}