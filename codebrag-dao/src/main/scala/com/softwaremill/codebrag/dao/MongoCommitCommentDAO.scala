package com.softwaremill.codebrag.dao

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import com.softwaremill.codebrag.domain.{CommentBase, InlineComment, CommitComment}
import net.liftweb.mongodb.record.field.{ObjectIdField, DateField}
import org.bson.types.ObjectId
import com.foursquare.rogue.LiftRogue._
import org.joda.time.DateTime
import net.liftweb.record.field.IntField

class MongoCommitCommentDAO extends CommitCommentDAO {

  override def save(comment: CommentBase) {
    CommentRecordBuilder.buildFrom(comment).save
  }

  override def findInlineCommentsForCommit(commitId: ObjectId) = {
    val inlineCommentsQuery = CommentRecord.where(_.commitId eqs commitId).and(_.fileName exists true).and(_.lineNumber exists true)
    inlineCommentsQuery.fetch().map(recordToInlineComment)
  }

  override def findCommentsForEntireCommit(commitId: ObjectId) = {
    val commitCommentsQuery = CommentRecord.where(_.commitId eqs commitId).and(_.fileName exists false).and(_.lineNumber exists false)
    commitCommentsQuery.fetch().map(recordToComment)
  }

  private def recordToComment(record: CommentRecord) = {
    CommitComment(
      record.id.get,
      record.commitId.get,
      record.authorId.get,
      record.message.get,
      new DateTime(record.date.get)
    )
  }

  private def recordToInlineComment(record: CommentRecord): InlineComment = {
    InlineComment(recordToComment(record), record.fileName.valueBox.get, record.lineNumber.valueBox.get)
  }

  private object CommentRecordBuilder {

    def buildFrom(comment: CommentBase) = {
      comment match {
        case c: InlineComment => inlineCommentToCommentRecord(c)
        case c: CommitComment => commentToCommentRecord(c)
      }
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
      commentToCommentRecord(inlineComment.commitComment)
        .fileName(inlineComment.fileName)
        .lineNumber(inlineComment.lineNumber)
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

  object fileName extends LongStringField(this) { override def optional_? = true }

  object lineNumber extends IntField(this) { override def optional_? = true }

}

object CommentRecord extends CommentRecord with MongoMetaRecord[CommentRecord] {
  override def collectionName = "commit_comments"
}