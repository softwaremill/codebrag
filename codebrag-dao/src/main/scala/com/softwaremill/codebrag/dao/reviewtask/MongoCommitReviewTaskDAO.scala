package com.softwaremill.codebrag.dao.reviewtask

import com.softwaremill.codebrag.domain.CommitReviewTask
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{ObjectIdPk, ObjectIdField}
import com.foursquare.rogue.LiftRogue._
import org.bson.types.ObjectId

class MongoCommitReviewTaskDAO extends CommitReviewTaskDAO {

  def save(reviewTask: CommitReviewTask) {
    val ifExistsQuery = CommitReviewTaskRecord.where(_.userId eqs reviewTask.userId).and(_.commitId eqs reviewTask.commitId).asDBObject
    CommitReviewTaskRecord.upsert(ifExistsQuery, toRecord(reviewTask))
  }

  def delete(task: CommitReviewTask) {
    CommitReviewTaskRecord.where(_.commitId eqs task.commitId).and(_.userId eqs task.userId).findAndDeleteOne()
  }

  def commitsPendingReviewFor(userId: ObjectId) = {
    val userReviewTasks = CommitReviewTaskRecord.where(_.userId eqs userId).fetch()
    userReviewTasks.map(_.commitId.get).toSet
  }

  private def toRecord(commitToReview: CommitReviewTask) = {
    val dbo = CommitReviewTaskRecord.createRecord
      .commitId(commitToReview.commitId)
      .userId(commitToReview.userId)
    .asDBObject
    dbo.removeField("_id")
    dbo
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
