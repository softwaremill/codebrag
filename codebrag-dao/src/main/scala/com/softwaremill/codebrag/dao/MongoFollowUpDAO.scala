package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.FollowUp
import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord, MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import com.foursquare.rogue.LiftRogue._

class MongoFollowUpDAO extends FollowUpDAO {

  def createOrUpdateExisting(followUp: FollowUp) {
    val query = FollowUpRecord.where(_.user_id eqs followUp.userId).and(_.commit.subselect(_.id) eqs followUp.commit.id).asDBObject
    FollowUpRecord.upsert(query, followUpToRecord(followUp))
  }


  def followUpToRecord(followUp: FollowUp) = {
    val record = toFollowUpRecord(followUp).asDBObject
    record.removeField("_id")   // remove _id field, otherwise Mongo screams it cannot modify _id when updating record
    record
  }

  private def toFollowUpRecord(followUp: FollowUp): FollowUpRecord = {

    val commitInfo = FollowUpCommitInfoRecord.createRecord
      .id(followUp.commit.id)
      .message(followUp.commit.message)
      .author(followUp.commit.authorName)
      .date(followUp.commit.date.toDate)

    FollowUpRecord.createRecord
      .commit(commitInfo)
      .user_id(followUp.userId)
      .date(followUp.date.toDate)
  }

}

class FollowUpRecord extends MongoRecord[FollowUpRecord] with ObjectIdPk[FollowUpRecord] {
  def meta = FollowUpRecord

  object user_id extends ObjectIdField(this)
  object commit extends BsonRecordField(this, FollowUpCommitInfoRecord)
  object date extends DateField(this)
}

object FollowUpRecord extends FollowUpRecord with MongoMetaRecord[FollowUpRecord] {
  override def collectionName: String = "follow_ups"
}

class FollowUpCommitInfoRecord extends BsonRecord[FollowUpCommitInfoRecord] {
  def meta = FollowUpCommitInfoRecord

  object id extends ObjectIdField(this)
  object message extends LongStringField(this)
  object author extends LongStringField(this)
  object date extends DateField(this)

}

object FollowUpCommitInfoRecord extends FollowUpCommitInfoRecord with BsonMetaRecord[FollowUpCommitInfoRecord]
