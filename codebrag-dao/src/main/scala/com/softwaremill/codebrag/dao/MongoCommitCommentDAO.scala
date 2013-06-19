package com.softwaremill.codebrag.dao

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import com.softwaremill.codebrag.domain.{Like, Comment}
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
    val baseQuery = CommentRecord.where(_.commitId eqs comment.commitId)
    val query = (comment.fileName, comment.lineNumber) match {
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

class MongoLikeDAO extends LikeDAO {

  override def save(like: Like) {
    LikeToRecordBuilder.buildFrom(like).save
  }

  override def findLikesForCommit(commitId: ObjectId) = {
    val likes = LikeRecord.where(_.commitId eqs commitId).orderAsc(_.date).fetch()
    likes.map(RecordToLikeBuilder.buildFrom(_))
  }

  def findAllLikesInThreadWith(like: Like) = {
    val query = (like.fileName, like.lineNumber) match {
      case (Some(fileName), Some(lineNumber)) => {
        LikeRecord.where(_.commitId eqs like.commitId).and(_.fileName eqs fileName).and(_.lineNumber eqs lineNumber)
      }
      case _ => {
        LikeRecord.where(_.commitId eqs like.commitId).and(_.fileName exists false).and(_.lineNumber exists false)
      }
    }
    query.fetch().map(RecordToLikeBuilder.buildFrom(_))
  }

  private object LikeToRecordBuilder {

    def buildFrom(like: Like) = {
      LikeRecord.createRecord
        .id(like.id)
        .commitId(like.commitId)
        .authorId(like.authorId)
        .date(like.postingTime.toDate)
        .fileName(like.fileName)
        .lineNumber(like.lineNumber)
    }

  }

  private object RecordToLikeBuilder {

    def buildFrom(record: LikeRecord) = {
      Like(
        record.id.get,
        record.commitId.get,
        record.authorId.get,
        new DateTime(record.date.get),
        record.fileName.get,
        record.lineNumber.get)
    }

  }
}


trait UserReactionRecord[MyType <: MongoRecord[MyType]] {

  self: MongoRecord[MyType] =>

  object id extends ObjectIdField(self.asInstanceOf[MyType])

  object commitId extends ObjectIdField(self.asInstanceOf[MyType])

  object authorId extends ObjectIdField(self.asInstanceOf[MyType])

  object date extends DateField(self.asInstanceOf[MyType])

  object fileName extends OptionalLongStringField(self.asInstanceOf[MyType])

  object lineNumber extends OptionalIntField(self.asInstanceOf[MyType])

}



class CommentRecord extends MongoRecord[CommentRecord] with UserReactionRecord[CommentRecord] {

  def meta = CommentRecord

  object message extends LongStringField(this)

}

object CommentRecord extends CommentRecord with MongoMetaRecord[CommentRecord] {
  override def collectionName = "commit_comments"
}



class LikeRecord extends MongoRecord[LikeRecord] with UserReactionRecord[LikeRecord] {

  def meta = LikeRecord

}

object LikeRecord extends LikeRecord with MongoMetaRecord[LikeRecord] {
  override def collectionName = "commit_likes"
}