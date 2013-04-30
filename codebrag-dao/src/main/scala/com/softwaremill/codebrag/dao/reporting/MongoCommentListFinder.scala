package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.{UserDAO, MongoCommitCommentDAO, UserRecord}
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.domain.{CommentBase, InlineCommitComment, EntireCommitComment}

class MongoCommentListFinder(userDao: UserDAO) extends CommentListFinder {


  def commentsForCommit(commitId: ObjectId): CommentsView = {
    val dao = new MongoCommitCommentDAO
    val comments = dao.findCommentsForEntireCommit(commitId)
    val inlineComments = dao.findInlineCommentsForCommit(commitId)
    CommentsView(comments = mapCommitCommentsToView(comments), inlineComments = mapInlineCommitCommentsToView(inlineComments))
  }

  private def mapInlineCommitCommentsToView(comments: List[InlineCommitComment]): List[FileCommentsView] = {
    val commentersCached = findAllCommentersFor(comments)
    val byFiles = comments.groupBy(_.fileName)
    val byFileAndLineNumber = byFiles.map(file => (file._1, file._2.groupBy(_.lineNumber)))

    byFileAndLineNumber.map(forFile => {
      val linesMap = scala.collection.mutable.Map[Int, List[SingleCommentView]]()
      forFile._2.map(forLine => {
        linesMap(forLine._1) = forLine._2.map({ line =>
          val authorName = findCommenterName(commentersCached, line.authorId)
          SingleCommentView(line.id.toString, authorName, line.message, line.postingTime.toDate)
        })
      }).toList
      FileCommentsView(forFile._1, linesMap.toMap)
    }).toList
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
