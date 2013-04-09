package com.softwaremill.codebrag.dao

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import com.softwaremill.codebrag.domain.CommitComment
import net.liftweb.mongodb.record.field.{ObjectIdField, DateField}
import org.bson.types.ObjectId
import com.foursquare.rogue.LiftRogue._
import org.joda.time.DateTime

class MongoCommitCommentDAO extends CommitCommentDAO {

  override def save(comment: CommitComment) {
    commentToCommentRecord(comment).save
  }

  override def findAllForCommit(commitId: ObjectId) = {
    CommentRecord.where(_.commitId eqs commitId).fetch().map(commentRecordToComment)
  }

  private def commentToCommentRecord(comment: CommitComment): CommentRecord = {
    CommentRecord.createRecord
      .id(comment.id)
      .commitId(comment.commitId)
      .authorId(comment.authorId)
      .message(comment.message)
      .date(comment.postingTime.toDate)
  }

  private def commentRecordToComment(record: CommentRecord): CommitComment = {
    CommitComment(
      record.id.get,
      record.commitId.get,
      record.authorId.get,
      record.message.get,
      new DateTime(record.date.get)
    )
  }
}

class CommentRecord extends MongoRecord[CommentRecord] {
  def meta = CommentRecord

  object id extends ObjectIdField(this)

  object commitId extends ObjectIdField(this)

  object authorId extends ObjectIdField(this)

  object message extends LongStringField(this)

  object date extends DateField(this)

}

object CommentRecord extends CommentRecord with MongoMetaRecord[CommentRecord] {
  override def collectionName = "commit_comments"
}
