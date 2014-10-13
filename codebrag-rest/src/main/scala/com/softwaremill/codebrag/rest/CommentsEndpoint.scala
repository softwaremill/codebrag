package com.softwaremill.codebrag.rest

import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.comments.command.IncomingComment
import com.softwaremill.codebrag.dao.finders.views.CommentView
import com.softwaremill.codebrag.domain.Comment
import com.softwaremill.codebrag.usecases.reactions.AddCommentUseCase


trait CommentsEndpoint extends JsonServletWithAuthentication with UserReactionParametersReader {

  def addCommentUseCase: AddCommentUseCase

  post("/:id/comments") {
    haltIfNotAuthenticated()
    addCommentUseCase.execute(incomingComment) match {
      case Right(comment) => commentToView(comment)
      case Left(err) => halt(400, "Could not add comment to commit")
    }    
  }
  
  private def commentToView(comment: Comment) = {
    AddCommentResponse(CommentView(comment.id.toString, user.name, user.id.toString, comment.message, comment.postingTime.toDate, user.settings.avatarUrl))
  }

  private def incomingComment = {
    val params = readReactionParamsFromRequest
    val commentBody = extractNotEmptyString("body")

    IncomingComment(new ObjectId(params.commitId), user.id, commentBody, params.fileName, params.lineNumber)
  }
}

case class AddCommentResponse(comment: CommentView)
