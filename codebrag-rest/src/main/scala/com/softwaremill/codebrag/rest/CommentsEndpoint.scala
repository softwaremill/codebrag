package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.comments.command.{NewInlineComment, NewWholeCommitComment}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.{CommentListFinder, CommentListDTO, CommentListItemDTO}
import org.scalatra.swagger.SwaggerSupport
import com.softwaremill.codebrag.activities.CommentActivity
import com.softwaremill.codebrag.dao.UserDAO

trait CommentsEndpoint extends JsonServletWithAuthentication with CommentsEndpointSwaggerDefinition {

  def commentActivity: CommentActivity
  def userDao: UserDAO
  def commentListFinder: CommentListFinder

  post("/:id/comments", operation(addCommentOperation)) {

    haltIfNotAuthenticated()
    val savedComment = commentActivity.putCommentOnCommit(extractComment)
    userDao.findById(savedComment.authorId) match {
      case Some(user) => AddCommentResponse(CommentListItemDTO(savedComment.id.toString, user.name, savedComment.message, savedComment.postingTime.toDate))
      case None => halt(400, s"Invalid user id $savedComment.authorId")
    }
  }

  get("/:id/comments", operation(getCommentsOperation)) {
    haltIfNotAuthenticated()
    val commitId = params("id")
    commentListFinder.findAllForCommit(new ObjectId(commitId))
  }

  def extractComment = {
    val messageBody = extractNotEmptyString("body")
    val fileNameOpt = (parsedBody \ "fileName").extractOpt[String]
    val lineNumberOpt = (parsedBody \ "lineNumber").extractOpt[Int]
    (fileNameOpt, lineNumberOpt) match {
      case (None, None) => NewWholeCommitComment(new ObjectId(params("id")), new ObjectId(user.id), messageBody)
      case (Some(fileName), Some(lineNumber)) => {
        val comment = NewWholeCommitComment(new ObjectId(params("id")), new ObjectId(user.id), messageBody)
        NewInlineComment(comment, fileName, lineNumber)
      }
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

  val getCommentsOperation = apiOperation[CommentListDTO]("getList")
    .summary("Get a lists of comments")
    .parameter(pathParam[String]("id").description("Commit identifier").required)

}


case class AddCommentResponse(comment: CommentListItemDTO)