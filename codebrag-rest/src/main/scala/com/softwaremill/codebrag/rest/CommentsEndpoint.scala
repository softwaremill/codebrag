package com.softwaremill.codebrag.rest

import org.bson.types.ObjectId
import org.scalatra.swagger.SwaggerSupport
import com.softwaremill.codebrag.activities.AddCommentActivity
import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.service.comments.command.IncomingComment
import scala.Some
import com.softwaremill.codebrag.dao.reporting.views.CommentView


trait CommentsEndpoint extends JsonServletWithAuthentication with UserReactionParametersReader with CommentsEndpointSwaggerDefinition {

  def commentActivity: AddCommentActivity
  def userDao: UserDAO

  post("/:id/comments", operation(addCommentOperation)) {
    haltIfNotAuthenticated()
    val comment = buildIncomingComment
    val savedComment = commentActivity.addCommentToCommit(comment)
    userDao.findById(savedComment.authorId) match {
      case Some(user) => AddCommentResponse(CommentView(savedComment.id.toString, user.name, user.id.toString, savedComment.message, savedComment.postingTime.toDate, user.settings.avatarUrl))
      case None => halt(400, s"Invalid user id $savedComment.authorId")
    }
  }

  private def buildIncomingComment = {
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
