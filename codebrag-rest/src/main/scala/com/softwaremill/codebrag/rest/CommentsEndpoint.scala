package com.softwaremill.codebrag.rest

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting._
import org.scalatra.swagger.SwaggerSupport
import com.softwaremill.codebrag.activities.AddCommentActivity
import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.service.comments.command.NewInlineCommitComment
import scala.Some
import com.softwaremill.codebrag.service.comments.command.NewEntireCommitComment
import com.softwaremill.codebrag.dao.reporting.views.SingleCommentView

trait CommentsEndpoint extends JsonServletWithAuthentication with CommentsEndpointSwaggerDefinition {

  def commentActivity: AddCommentActivity
  def userDao: UserDAO
  def commentListFinder: CommentFinder

  post("/:id/comments", operation(addCommentOperation)) {

    haltIfNotAuthenticated()
    val savedComment = commentActivity.addCommentToCommit(extractComment)
    userDao.findById(savedComment.authorId) match {
      case Some(user) => AddCommentResponse(SingleCommentView(savedComment.id.toString, user.name, savedComment.message, savedComment.postingTime.toDate, user.avatarUrl))
      case None => halt(400, s"Invalid user id $savedComment.authorId")
    }
  }

  def extractComment = {
    val messageBody = extractNotEmptyString("body")
    val fileNameOpt = (parsedBody \ "fileName").extractOpt[String]
    val lineNumberOpt = (parsedBody \ "lineNumber").extractOpt[Int]
    val commitIdParam = params("id")
    (fileNameOpt, lineNumberOpt) match {
      case (None, None) => NewEntireCommitComment(new ObjectId(commitIdParam), new ObjectId(user.id), messageBody)
      case (Some(fileName), Some(lineNumber)) => NewInlineCommitComment(new ObjectId(commitIdParam), new ObjectId(user.id), messageBody, fileName, lineNumber)
      case _ => halt(400, "File name and line number must be present for inline comment")
    }
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

case class AddCommentResponse(comment: SingleCommentView)
