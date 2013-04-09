package com.softwaremill.codebrag.service.comments

import com.softwaremill.codebrag.dao.CommitCommentDAO
import com.softwaremill.codebrag.domain.CommitComment
import command.AddComment
import pl.softwaremill.common.util.time.Clock
import com.softwaremill.codebrag.common.IdGenerator

class CommentService(commentDao: CommitCommentDAO)(implicit idGenerator: IdGenerator, clock: Clock) {

  def addCommentToCommit(command: AddComment): CommitComment = {

    val commitId = command.commitId
    val newCommentId = idGenerator.generateRandom()
    val time = clock.currentDateTimeUTC()
    val newComment = CommitComment(newCommentId, commitId, command.authorId, command.message, time)
    commentDao.save(newComment)
    newComment
  }
}