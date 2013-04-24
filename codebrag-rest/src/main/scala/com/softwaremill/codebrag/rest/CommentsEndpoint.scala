package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.comments.command.{NewInlineCommitComment, NewEntireCommitComment}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.{CommentsView, CommentListFinder, CommentListDTO, CommentListItemDTO}
import org.scalatra.swagger.SwaggerSupport
import com.softwaremill.codebrag.activities.AddCommentActivity
import com.softwaremill.codebrag.dao.UserDAO
import scala.deprecated

trait CommentsEndpoint extends JsonServletWithAuthentication with CommentsEndpointSwaggerDefinition {

  def commentActivity: AddCommentActivity
  def userDao: UserDAO
  def commentListFinder: CommentListFinder

  post("/:id/comments", operation(addCommentOperation)) {

    haltIfNotAuthenticated()
    val savedComment = commentActivity.addCommentToCommit(extractComment)
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

  get("/:id/comments/v2", operation(getCommentsOperationV2)) { // will replace the one above when frontend is finished
    haltIfNotAuthenticated()
    val commitId = params("id")
    commentListFinder.commentsForCommit(new ObjectId(commitId))
  }

  def extractComment = {
    val messageBody = extractNotEmptyString("body")
    val fileNameOpt = (parsedBody \ "fileName").extractOpt[String]
    val lineNumberOpt = (parsedBody \ "lineNumber").extractOpt[Int]
    (fileNameOpt, lineNumberOpt) match {
      case (None, None) => NewEntireCommitComment(new ObjectId(params("id")), new ObjectId(user.id), messageBody)
      case (Some(fileName), Some(lineNumber)) => {
        val comment = NewEntireCommitComment(new ObjectId(params("id")), new ObjectId(user.id), messageBody)
        NewInlineCommitComment(comment, fileName, lineNumber)
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

  @deprecated("Will be removed on inline comments finish")
  val getCommentsOperation = apiOperation[CommentListDTO]("getList")
    .summary("Get a lists of comments")
    .parameter(pathParam[String]("id").description("Commit identifier").required)

  val getCommentsOperationV2 = apiOperation[CommentsView]("getList")
    .summary("Get a lists of comments")
    .parameter(pathParam[String]("id").description("Commit identifier").required)

}


case class AddCommentResponse(comment: CommentListItemDTO)