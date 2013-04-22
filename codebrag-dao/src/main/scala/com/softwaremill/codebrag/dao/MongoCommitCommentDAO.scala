package com.softwaremill.codebrag.dao

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import com.softwaremill.codebrag.domain.{InlineComment, CommitComment}
import net.liftweb.mongodb.record.field.{ObjectIdField, DateField}
import org.bson.types.ObjectId
import com.foursquare.rogue.LiftRogue._
import org.joda.time.DateTime
import net.liftweb.record.field.IntField

class MongoCommitCommentDAO extends CommitCommentDAO {

  override def save(comment: CommitComment) {
    commentToCommentRecord(comment).save
  }

  override def save(inlineComment: InlineComment) {
    inlineCommentToCommentRecord(inlineComment).save
  }

  override def findInlineCommentsForCommit(commitId: ObjectId) = {
    val inlineCommentsQuery = InlineCommentRecord.where(_.commitId eqs commitId).and(_.fileName exists true)
    inlineCommentsQuery.fetch().map(inlineCommentRecordToComment)
  }

  override def findCommentsForEntireCommit(commitId: ObjectId) = {
    val commitCommentsQuery = InlineCommentRecord.where(_.commitId eqs commitId).and(_.fileName exists false)
    commitCommentsQuery.fetch().map(commentRecordToComment)
  }

  private def commentRecordToComment[T <: BaseRecord[T]](record: BaseRecord[T]) = {
    CommitComment(
      record.id.get,
      record.commitId.get,
      record.authorId.get,
      record.message.get,
      new DateTime(record.date.get)
    )
  }

  private def inlineCommentRecordToComment(record: InlineCommentRecord): InlineComment = {
    InlineComment(commentRecordToComment(record), record.fileName.get, record.lineNumber.get)
  }

  private def commentToCommentRecord(comment: CommitComment) = {
    CommentRecord.createRecord
      .id(comment.id)
      .commitId(comment.commitId)
      .authorId(comment.authorId)
      .message(comment.message)
      .date(comment.postingTime.toDate)
  }

  private def inlineCommentToCommentRecord(inlineComment: InlineComment) = {

    // how to "reuse" everything that is defind in commentToCommentRecord() defined below?
    InlineCommentRecord.createRecord
      .fileName(inlineComment.fileName)
      .lineNumber(inlineComment.lineNumber)
      .id(inlineComment.comment.id)
      .commitId(inlineComment.comment.commitId)
      .authorId(inlineComment.comment.authorId)
      .message(inlineComment.comment.message)
      .date(inlineComment.comment.postingTime.toDate)
  }

}

trait BaseRecord[T <: BaseRecord[T]] extends MongoRecord[T] { self: T =>

  object id extends ObjectIdField(this)

  object commitId extends ObjectIdField(this)

  object authorId extends ObjectIdField(this)

  object message extends LongStringField(this)

  object date extends DateField(this)

}


class CommentRecord extends BaseRecord[CommentRecord] {

  def meta = CommentRecord

}

class InlineCommentRecord extends BaseRecord[InlineCommentRecord] {

  def meta = InlineCommentRecord

  object fileName extends LongStringField(this)

  object lineNumber extends IntField(this)

}

object CommentRecord extends CommentRecord with MongoMetaRecord[CommentRecord] {
  override def collectionName = "commit_comments"
}

object InlineCommentRecord extends InlineCommentRecord with MongoMetaRecord[InlineCommentRecord] {
  override def collectionName = "commit_comments"
}
