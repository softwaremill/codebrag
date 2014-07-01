package com.softwaremill.codebrag.rest

import org.bson.types.ObjectId
import org.scalatra.swagger.SwaggerSupport
import com.softwaremill.codebrag.usecases.AddCommentUseCase
import com.softwaremill.codebrag.service.comments.command.IncomingComment
import scala.Some
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.finders.views.CommentView
import com.softwaremill.codebrag.domain.Comment


trait CommentsEndpoint extends JsonServletWithAuthentication with UserReactionParametersReader with CommentsEndpointSwaggerDefinition {

  def addCommentUseCase: AddCommentUseCase

  post("/:id/comments", operation(addCommentOperation)) {
    haltIfNotAuthenticated()
    addCommentUseCase.execute(incomingComment) match {
      case Right(comment) => commentToView(comment)
      case Left(err) => halt(400, "Could not add comment to commit")
    }    
  }
  
  private def commentToView(comment: Comment) = {
    AddCommentResponse(CommentView(comment.id.toString, user.fullName, user.id.toString, comment.message, comment.postingTime.toDate, user.settings.avatarUrl))
  }

  private def incomingComment = {
    val params = readReactionParamsFromRequest
    val commentBody = extractNotEmptyString("body")

    IncomingComment(new ObjectId(params.commitId), new ObjectId(user.id), commentBody, params.fileName, params.lineNumber)
  }
}

trait CommentsEndpointSwaggerDefinition extends SwaggerSupport {

  val addCommentOperation = apiOperation[AddCommentResponse]("add")
    .summary("Posts a new comment")
    .parameter(pathParam[String]("id").description("Commit identifier").required)
    .parameter(bodyParam[String]("body").description("Message body").required)
    .parameter(bodyParam[String]("fileName").description("File name for inline comment").optional)
    .parameter(bodyParam[Int]("lineNumber").description("Line number of file for inline comment").optional)

}

case class AddCommentResponse(comment: CommentView)
