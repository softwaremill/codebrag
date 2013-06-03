package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.{UserDAO, MongoCommitCommentDAO, UserRecord}
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.domain.{CommentBase, InlineCommitComment, EntireCommitComment}
import com.softwaremill.codebrag.dao.reporting.views.{SingleCommentView, CommentsView}

class MongoCommentFinder(userDao: UserDAO) extends CommentFinder {


  def commentsForCommit(commitId: ObjectId): CommentsView = {
    val dao = new MongoCommitCommentDAO
    val comments = dao.findCommentsForEntireCommit(commitId)
    val inlineComments = dao.findInlineCommentsForCommit(commitId)
    CommentsView(comments = mapCommitCommentsToView(comments), inlineComments = mapInlineCommitCommentsToView(inlineComments))
  }

  private def mapInlineCommitCommentsToView(comments: List[InlineCommitComment]): Map[String, CommentsView.LineToCommentListMap] = {
    val commentersCached = findAllCommentersFor(comments)
    val byFiles = comments.groupBy(_.fileName)
    val byFileAndLineNumber = byFiles.map({
      case (fileName, fileComments) => (fileName, fileComments.groupBy(_.lineNumber))
    })

    byFileAndLineNumber.map({
      case (fileName, commentsForFile) =>
        (fileName, commentsForFile.map({
          case (lineNumber, lineComments) => (lineNumber,
            lineComments.map({
              line =>
                val authorName = findCommenterName(commentersCached, line.authorId)
                SingleCommentView(line.id.toString, authorName, line.message, line.postingTime.toDate)
            }))
        }))
    })
  }

  private def findAllCommentersFor(comments: List[CommentBase]): List[(ObjectId, String)] = {
    UserRecord.select(_.id, _.name).where(_.id in comments.map(_.authorId)).fetch()
  }

  private def findCommenterName(commenters: List[(ObjectId, String)], commenterId: ObjectId) = {
    commenters.find(_._1 == commenterId) match {
      case Some(author) => author._2
      case None => "Unknown author"
    }
  }

  private def mapCommitCommentsToView(comments: List[EntireCommitComment]) = {
    val commentersCached = findAllCommentersFor(comments)
    comments.map(comment => {
      val authorName = findCommenterName(commentersCached, comment.authorId)
      SingleCommentView(comment.id.toString, authorName, comment.message, comment.postingTime.toDate)
    })
  }

}
