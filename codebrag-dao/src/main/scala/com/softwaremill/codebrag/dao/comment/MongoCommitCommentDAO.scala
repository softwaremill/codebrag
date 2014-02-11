package com.softwaremill.codebrag.dao.comment

import com.softwaremill.codebrag.domain.{ThreadDetails, Comment}
import org.bson.types.ObjectId
import com.foursquare.rogue.LiftRogue._
import org.joda.time.DateTime
import com.softwaremill.codebrag.dao.user.CommentRecord

class MongoCommitCommentDAO extends CommitCommentDAO {

  override def save(comment: Comment) {
    CommentToRecordBuilder.buildFrom(comment).save
  }

  override def findCommentsForCommits(commitIds: ObjectId*) = {
    val comments = CommentRecord.where(_.commitId in commitIds).orderAsc(_.date).fetch()
    comments.map(RecordToCommentBuilder.buildFrom(_))
  }

  def findAllCommentsForThread(thread: ThreadDetails) = {
    val baseQuery = CommentRecord.where(_.commitId eqs thread.commitId)
    val query = (thread.fileName, thread.lineNumber) match {
      case (Some(fileName), Some(lineNumber)) => {
        baseQuery.and(_.fileName eqs fileName).and(_.lineNumber eqs lineNumber)
      }
      case _ => {
        baseQuery.and(_.fileName exists false).and(_.lineNumber exists false)
      }
    }
    query.fetch().map(RecordToCommentBuilder.buildFrom(_))
  }

  private object CommentToRecordBuilder {

    def buildFrom(comment: Comment) = {
      CommentRecord.createRecord
        .id(comment.id)
        .commitId(comment.commitId)
        .authorId(comment.authorId)
        .message(comment.message)
        .date(comment.postingTime.toDate)
        .fileName(comment.fileName)
        .lineNumber(comment.lineNumber)
    }

  }

  private object RecordToCommentBuilder {

    def buildFrom(record: CommentRecord) = {
      Comment(
        record.id.get,
        record.commitId.get,
        record.authorId.get,
        new DateTime(record.date.get),
        record.message.get,
        record.fileName.get,
        record.lineNumber.get)
    }

  }
}