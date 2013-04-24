package com.softwaremill.codebrag.dao

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import com.softwaremill.codebrag.domain.{CommentBase, InlineCommitComment, EntireCommitComment}
import net.liftweb.mongodb.record.field.{ObjectIdField, DateField}
import org.bson.types.ObjectId
import com.foursquare.rogue.LiftRogue._
import org.joda.time.DateTime
import net.liftweb.record.field.{EnumField, IntField}

class MongoCommitCommentDAO extends CommitCommentDAO {

  override def save(comment: CommentBase) {
    CommentToRecordBuilder.buildFrom(comment).save
  }

  override def findInlineCommentsForCommit(commitId: ObjectId) = {
    val inlineCommentsQuery = CommentRecord.where(_.commitId eqs commitId).and(_.commentType eqs CommentRecord.CommentType.Inline)
    inlineCommentsQuery.fetch().map(RecordToCommentBuilder.buildFrom(_).asInstanceOf[InlineCommitComment])
  }

  override def findCommentsForEntireCommit(commitId: ObjectId) = {
    val commitCommentsQuery = CommentRecord.where(_.commitId eqs commitId).and(_.commentType eqs CommentRecord.CommentType.Commit)
    commitCommentsQuery.fetch().map(RecordToCommentBuilder.buildFrom(_).asInstanceOf[EntireCommitComment])
  }

  def findAllCommentsInThreadWith(comment: CommentBase) = {
    val source = CommentToRecordBuilder.buildFrom(comment)
    val query = CommentRecord
      .where(_.commitId eqs source.commitId.get)
      .and(_.commentType eqs source.commentType.get)
      .andOpt(source.fileName.valueBox)(_.fileName eqs _)
      .andOpt(source.lineNumber.valueBox)(_.lineNumber eqs _)
    query.fetch().map(RecordToCommentBuilder.buildFrom(_))
  }

  private object CommentToRecordBuilder {

    def buildFrom(comment: CommentBase) = {
      comment match {
        case c: InlineCommitComment => inlineCommentToCommentRecord(c)
        case c: EntireCommitComment => commentToCommentRecord(c)
      }
    }

    private def commentToCommentRecord(comment: EntireCommitComment) = {
      CommentRecord.createRecord
        .id(comment.id)
        .commitId(comment.commitId)
        .authorId(comment.authorId)
        .message(comment.message)
        .date(comment.postingTime.toDate)
        .commentType(CommentRecord.CommentType.Commit)
    }

    private def inlineCommentToCommentRecord(inlineComment: InlineCommitComment) = {
      commentToCommentRecord(inlineComment.commitComment)
        .fileName(inlineComment.fileName)
        .lineNumber(inlineComment.lineNumber)
        .commentType(CommentRecord.CommentType.Inline)
    }

  }

  private object RecordToCommentBuilder {

    import CommentRecord.CommentType._

    def buildFrom(comment: CommentRecord) = {
      comment.commentType.get match {
        case Inline => recordToInlineComment(comment)
        case Commit => recordToComment(comment)
      }
    }

    private def recordToComment(record: CommentRecord) = {
      EntireCommitComment(
        record.id.get,
        record.commitId.get,
        record.authorId.get,
        record.message.get,
        new DateTime(record.date.get)
      )
    }

    private def recordToInlineComment(record: CommentRecord): InlineCommitComment = {
      InlineCommitComment(recordToComment(record), record.fileName.valueBox.get, record.lineNumber.valueBox.get)
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

  object commentType extends EnumField(this, CommentRecord.CommentType)

}

object CommentRecord extends CommentRecord with MongoMetaRecord[CommentRecord] {
  override def collectionName = "commit_comments"

  object CommentType extends Enumeration {
    type CommentType = Value
    val Commit, Inline = Value
  }
}