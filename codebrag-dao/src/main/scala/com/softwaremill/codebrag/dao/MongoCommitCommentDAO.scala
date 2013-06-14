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
//    val source = CommentToRecordBuilder.buildFrom(comment)
//    val query = CommentRecord
//      .where(_.commitId eqs source.commitId.get)
//      .and(_.commentType eqs source.commentType.get)
//      .andOpt(source.fileName.valueBox)(_.fileName eqs _)
//      .andOpt(source.lineNumber.valueBox)(_.lineNumber eqs _)
//    query.fetch().map(RecordToCommentBuilder.buildFrom(_))
    List.empty
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