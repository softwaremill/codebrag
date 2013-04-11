package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.comments.command.AddComment
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
    haltIfNotAuthenticated
    val commitId = params("id")
    val messageBody = extractNotEmptyString("body")
    val command = AddComment(new ObjectId(commitId), user.id, messageBody)
    val newComment = commentActivity.commentOnCommit(command)
    userDao.findById(command.authorId) match {
      case Some(user) => AddCommentResponse(CommentListItemDTO(newComment.id.toString, user.name, command.message, newComment.postingTime.toDate))
      case None => halt(400, s"Invalid user id $command.authorId")
    }
  }

  get("/:id/comments", operation(getCommentsOperation)) {
    haltIfNotAuthenticated
    val commitId = params("id")
    commentListFinder.findAllForCommit(new ObjectId(commitId))
  }

}

trait CommentsEndpointSwaggerDefinition extends SwaggerSupport {

  val addCommentOperation = apiOperation[AddCommentResponse]("add")
    .summary("Posts a new comment")
    .parameter(pathParam[String]("id").description("Commit identifier").required)
    .parameter(bodyParam[String]("body").description("Message body").required)

  val getCommentsOperation = apiOperation[CommentListDTO]("getList")
    .summary("Get a lists of comments")
    .parameter(pathParam[String]("id").description("Commit identifier").required)

}


case class AddCommentResponse(comment: CommentListItemDTO)