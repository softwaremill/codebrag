package com.softwaremill.codebrag.service.comments

import com.softwaremill.codebrag.dao.{CommitReviewDAO, UserDAO}
import com.softwaremill.codebrag.domain.CommitReview
import command.AddComment
import pl.softwaremill.common.util.time.Clock
import com.softwaremill.codebrag.common.IdGenerator
import com.softwaremill.codebrag.dao.reporting.CommentListItemDTO

class CommentService(reviewDAO: CommitReviewDAO, userDAO: UserDAO)(implicit idGenerator: IdGenerator, clock: Clock) {

  def addCommentToCommit(command: AddComment): CommentListItemDTO = {

    val commitId = command.commitId
    val reviewOpt = reviewDAO.findById(commitId)
    val user = userDAO.findById(command.authorId).get
    val newCommentId = idGenerator.generateRandom()
    val time = clock.currentDateTimeUTC()
    val reviewWithComment = reviewOpt match {
      case Some(review) => review.addComment(newCommentId, user.id, command.message, time)
      case None => CommitReview.createWithComment(commitId, newCommentId, user.id, command.message, time)
    }
    reviewDAO.save(reviewWithComment)
    CommentListItemDTO(newCommentId.toString, user.name, command.message, time.toDate)
  }
}