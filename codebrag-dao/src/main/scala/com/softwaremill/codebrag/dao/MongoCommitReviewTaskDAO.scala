package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.CommitReviewTask
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{ObjectIdPk, ObjectIdField}
import com.foursquare.rogue.LiftRogue._


class MongoCommitReviewTaskDAO extends CommitReviewTaskDAO {

  def save(reviewTask: CommitReviewTask) {
    val ifExistsQuery = CommitReviewTaskRecord.where(_.userId eqs reviewTask.userId).and(_.commitId eqs reviewTask.commitId).asDBObject
    CommitReviewTaskRecord.upsert(ifExistsQuery, toRecord(reviewTask).asDBObject)
  }

  def delete(task: CommitReviewTask) {
    CommitReviewTaskRecord.where(_.commitId eqs task.commitId).and(_.userId eqs task.userId).findAndDeleteOne()
  }

  private def toRecord(commitToReview: CommitReviewTask) = {
    CommitReviewTaskRecord.createRecord
      .commitId(commitToReview.commitId)
      .userId(commitToReview.userId)
  }

}

class CommitReviewTaskRecord extends MongoRecord[CommitReviewTaskRecord] with ObjectIdPk[CommitReviewTaskRecord] {
  def meta = CommitReviewTaskRecord
  object commitId extends ObjectIdField(this)
  object userId extends ObjectIdField(this)
}

object CommitReviewTaskRecord extends CommitReviewTaskRecord with MongoMetaRecord[CommitReviewTaskRecord] {
  override def collectionName = "commit_review_tasks"
}
