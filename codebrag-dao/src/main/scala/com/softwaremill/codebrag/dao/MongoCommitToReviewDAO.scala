package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.CommitToReview
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{ObjectIdPk, ObjectIdField}
import com.foursquare.rogue.LiftRogue._


class MongoCommitToReviewDAO extends CommitToReviewDAO {

  def save(commitToReview: CommitToReview) {
    val ifExistsQuery = CommitToReviewRecord.where(_.userId eqs commitToReview.userId).and(_.commitId eqs commitToReview.commitId).asDBObject
    CommitToReviewRecord.upsert(ifExistsQuery, toRecord(commitToReview).asDBObject)
  }

  private def toRecord(commitToReview: CommitToReview) = {
    CommitToReviewRecord.createRecord
      .commitId(commitToReview.commitId)
      .userId(commitToReview.userId)
  }

}

class CommitToReviewRecord extends MongoRecord[CommitToReviewRecord] with ObjectIdPk[CommitToReviewRecord] {
  def meta = CommitToReviewRecord
  object commitId extends ObjectIdField(this)
  object userId extends ObjectIdField(this)
}

object CommitToReviewRecord extends CommitToReviewRecord with MongoMetaRecord[CommitToReviewRecord] {
  override def collectionName = "commits_to_review"
}
