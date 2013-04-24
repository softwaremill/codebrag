package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.{UserDAO, MongoCommitCommentDAO, CommentRecord, UserRecord}
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.domain.{CommentBase, InlineCommitComment, EntireCommitComment}

// null - for step-by-step refactoring, will remove soon
class MongoCommentListFinder(userDao: UserDAO = null) extends CommentListFinder {


  override def findAllForCommit(commitId: ObjectId) = {
    val commentRecords = CommentRecord.where(_.commitId eqs commitId).fetch()
    CommentListDTO(buildCommentsFromRecord(commentRecords))
  }

  private def buildCommentsFromRecord(records: List[CommentRecord]): List[CommentListItemDTO] = {

    def buildCommentItem(record: CommentRecord, namesGroupedById: Map[ObjectId, String]): CommentListItemDTO = {
      val userName = namesGroupedById.getOrElse(record.authorId.get, "Unknown user")
      CommentListItemDTO(record.id.get.toString, userName, record.message.get, record.date.get)
    }

    val comments = records.sortBy(_.date.get)
    val authorIdSet = comments.map(_.authorId.get).toSet
    val idNamesPairs = UserRecord.select(_.id, _.name).where(_.id in authorIdSet).fetch()
    val namesGroupedById = idNamesPairs.toMap
    comments.map(buildCommentItem(_, namesGroupedById))
  }

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
      val lines = forFile._2.map(forLine => {
        LineCommentsView(forLine._1, forLine._2.map({ line =>
          val authorName = findCommenterName(commentersCached, line.commitComment.authorId)
          SingleCommentView(line.commitComment.id.toString, authorName, line.commitComment.message, line.commitComment.postingTime.toDate)
        }))
      }).toList
      FileCommentsView(forFile._1, lines)
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
