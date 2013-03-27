package com.softwaremill.codebrag.service.comments

import com.softwaremill.codebrag.dao.{CommitReviewDAO, UserDAO, CommitInfoDAO}
import com.softwaremill.codebrag.domain.{CommitReview, User, CommitComment, CommitInfo}
import command.AddComment
import pl.softwaremill.common.util.time.Clock
import com.softwaremill.codebrag.common.IdGenerator
import org.bson.types.ObjectId

class CommentService(reviewDAO: CommitReviewDAO, userDAO: UserDAO)(implicit idGenerator: IdGenerator, clock: Clock) {

  def addCommentToCommit(command: AddComment) = {

    val commitId = command.commitId
    val reviewOpt = reviewDAO.findById(commitId)
    val user = userDAO.findByLoginOrEmail(command.authorLogin).get
    val newCommentId = idGenerator.generateRandom()
    val reviewWithComment = reviewOpt match {
      case Some(review) => review.addComment(newCommentId, user.id, command.message, clock)
      case None => CommitReview.createWithComment(commitId, newCommentId, user.id, command.message, clock)
    }
    reviewDAO.save(reviewWithComment)
    newCommentId
  }
}