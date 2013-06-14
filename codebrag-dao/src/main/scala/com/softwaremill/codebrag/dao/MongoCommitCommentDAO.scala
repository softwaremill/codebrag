package com.softwaremill.codebrag.dao

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import com.softwaremill.codebrag.domain.Comment
import net.liftweb.mongodb.record.field.{ObjectIdField, DateField}
import org.bson.types.ObjectId
import com.foursquare.rogue.LiftRogue._
import org.joda.time.DateTime
import net.liftweb.record.field.OptionalIntField


class MongoCommitCommentDAO extends CommitCommentDAO {

  override def save(comment: Comment) {
    CommentToRecordBuilder.buildFrom(comment).save
  }

  override def findCommentsForCommit(commitId: ObjectId) = {
    val comments = CommentRecord.where(_.commitId eqs commitId).orderAsc(_.date).fetch()
    comments.map(RecordToCommentBuilder.buildFrom(_))
  }

  def findAllCommentsInThreadWith(comment: Comment) = {
    val query = (comment.fileName, comment.lineNumber) match {
      case (Some(fileName), Some(lineNumber)) => {
        CommentRecord.where(_.commitId eqs comment.commitId).and(_.fileName eqs fileName).and(_.lineNumber eqs lineNumber)
      }
      case _ => {
        CommentRecord.where(_.commitId eqs comment.commitId).and(_.fileName exists false).and(_.lineNumber exists false)
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
        record.fileName.value,
        record.lineNumber.value)
    }

  }
}

class CommentRecord extends MongoRecord[CommentRecord] {

  def meta = CommentRecord

  object id extends ObjectIdField(this)

  object commitId extends ObjectIdField(this)

  object authorId extends ObjectIdField(this)

  object message extends LongStringField(this)

  object date extends DateField(this)

  object fileName extends OptionalLongStringField(this)

  object lineNumber extends OptionalIntField(this)

}

object CommentRecord extends CommentRecord with MongoMetaRecord[CommentRecord] {
  override def collectionName = "commit_comments"
}