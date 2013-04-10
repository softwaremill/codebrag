package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.Followup
import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord, MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import com.foursquare.rogue.LiftRogue._
import org.bson.types.ObjectId

class MongoFollowupDAO extends FollowupDAO {

  def createOrUpdateExisting(followup: Followup) {
    val query = FollowupRecord.where(_.user_id eqs followup.userId).and(_.commit.subselect(_.id) eqs followup.commit.id).asDBObject
    FollowupRecord.upsert(query, followupToRecord(followup))
  }

  override def delete(commitId: ObjectId, userId: ObjectId) {
    FollowupRecord.where(_.commit.subselect(_.id) eqs commitId).and(_.user_id eqs userId).findAndDeleteOne()
  }

  def followupToRecord(followup: Followup) = {
    val record = toFollowupRecord(followup).asDBObject
    record.removeField("_id")   // remove _id field, otherwise Mongo screams it cannot modify _id when updating record
    record
  }

  private def toFollowupRecord(followup: Followup): FollowupRecord = {

    val commitInfo = FollowupCommitInfoRecord.createRecord
      .id(followup.commit.id)
      .message(followup.commit.message)
      .author(followup.commit.authorName)
      .date(followup.commit.date.toDate)

    FollowupRecord.createRecord
      .commit(commitInfo)
      .user_id(followup.userId)
      .date(followup.date.toDate)
  }


}

class FollowupRecord extends MongoRecord[FollowupRecord] with ObjectIdPk[FollowupRecord] {
  def meta = FollowupRecord

  object user_id extends ObjectIdField(this)
  object commit extends BsonRecordField(this, FollowupCommitInfoRecord)
  object date extends DateField(this)
}

object FollowupRecord extends FollowupRecord with MongoMetaRecord[FollowupRecord] {
  override def collectionName = "follow_ups"
}

class FollowupCommitInfoRecord extends BsonRecord[FollowupCommitInfoRecord] {
  def meta = FollowupCommitInfoRecord

  object id extends ObjectIdField(this)
  object message extends LongStringField(this)
  object author extends LongStringField(this)
  object date extends DateField(this)

}

object FollowupCommitInfoRecord extends FollowupCommitInfoRecord with BsonMetaRecord[FollowupCommitInfoRecord]
