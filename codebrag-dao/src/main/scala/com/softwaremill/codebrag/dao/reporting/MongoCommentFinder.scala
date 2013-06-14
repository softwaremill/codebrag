package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.{UserDAO, MongoCommitCommentDAO, UserRecord}
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.domain.Comment
import com.softwaremill.codebrag.dao.reporting.views.{SingleCommentView, CommentsView}

class MongoCommentFinder(userDao: UserDAO) extends CommentFinder {


  def commentsForCommit(commitId: ObjectId): CommentsView = {
    val dao = new MongoCommitCommentDAO
    val comments = dao.findCommentsForCommit(commitId)
    val (inlineComments, entireComments) = comments.partition(c => c.fileName.isDefined && c.lineNumber.isDefined)
    CommentsView(comments = mapCommitCommentsToView(entireComments), inlineComments = mapInlineCommitCommentsToView(inlineComments))
  }

  private def mapInlineCommitCommentsToView(comments: List[Comment]): Map[String, CommentsView.LineToCommentListMap] = {
    val commentersCached = findAllCommentersFor(comments)
    val byFiles = comments.groupBy(_.fileName.get)
    val byFileAndLineNumber = byFiles.map({
      case (fileName, fileComments) => (fileName, fileComments.groupBy(_.lineNumber.get))
    })

    byFileAndLineNumber.map({
      case (fileName, commentsForFile) =>
        (fileName, commentsForFile.map({
          case (lineNumber, lineComments) => (lineNumber,
            lineComments.map({
              line =>
                val (authorName, avatarUrl) = findCommenterDetails(commentersCached, line.authorId)
                SingleCommentView(line.id.toString, authorName, line.message, line.postingTime.toDate, avatarUrl)
            }))
        }))
    })
  }

  private def findAllCommentersFor(comments: List[Comment]): List[(ObjectId, String, String)] = {
    UserRecord.select(_.id, _.name, _.avatarUrl).where(_.id in comments.map(_.authorId)).fetch()
  }

  private def findCommenterDetails(commenters: List[(ObjectId, String, String)], commenterId: ObjectId) = {
    commenters.find(_._1 == commenterId) match {
      case Some(author) => (author._2, author._3)
      case None => ("Unknown author", "")
    }
  }

  private def mapCommitCommentsToView(comments: List[Comment]) = {
    val commentersCached = findAllCommentersFor(comments)
    comments.map(comment => {
      val (authorName, avatarUrl) = findCommenterDetails(commentersCached, comment.authorId)
      SingleCommentView(comment.id.toString, authorName, comment.message, comment.postingTime.toDate, avatarUrl)
    })
  }

}
