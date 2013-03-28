package com.softwaremill.codebrag.dao

import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{CommitComment, CommitReview}
import net.liftweb.mongodb.record.{MongoRecord, MongoMetaRecord, BsonMetaRecord, BsonRecord}
import net.liftweb.mongodb.record.field.{ObjectIdPk, BsonRecordListField, ObjectIdField, DateField}
import org.joda.time.DateTime


class MongoCommitReviewDAO extends CommitReviewDAO {

  override def findById(id: ObjectId): Option[CommitReview] = {
    toReviewOption(CommitReviewRecord.find(id).toOption)
  }

  override def save(review: CommitReview)
  {
    reviewToRecord(review).save
  }

  private def reviewToRecord(review: CommitReview): CommitReviewRecord = {
    CommitReviewRecord.createRecord
      .id(review.commitId)
      .comments(review.comments.map(commentToCommentRecord(_)))
  }

  private def toReviewOption(recordOption: Option[CommitReviewRecord]): Option[CommitReview] = {
    recordOption.map(record =>
      CommitReview(record.id.get, record.comments.get.map(commentRecordToComment(_))))
  }

  private def commentRecordToComment(record: CommentRecord): CommitComment = {
    CommitComment(record.id.get, record.authorId.get, record.message.get, new DateTime(record.date.get))
  }

  private def commentToCommentRecord(comment: CommitComment): CommentRecord = {
    CommentRecord.createRecord
    .id(comment.id)
    .authorId(comment.authorId)
    .message(comment.message)
    .date(comment.postingTime.toDate)
  }

}

class CommitReviewRecord extends MongoRecord[CommitReviewRecord] with ObjectIdPk[CommitReviewRecord] {
  def meta = CommitReviewRecord

  object comments extends BsonRecordListField(this, CommentRecord)
}

object CommitReviewRecord extends CommitReviewRecord with MongoMetaRecord[CommitReviewRecord] {
  override def collectionName: String = "commitReviews"
}

class CommentRecord extends BsonRecord[CommentRecord] {
  def meta = CommentRecord

  object id extends ObjectIdField(this)

  object authorId extends ObjectIdField(this)

  object message extends LongStringField(this)

  object date extends DateField(this)

}

object CommentRecord extends CommentRecord with BsonMetaRecord[CommentRecord]  {
}
