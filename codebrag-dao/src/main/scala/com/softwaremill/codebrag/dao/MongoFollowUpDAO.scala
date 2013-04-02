package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.FollowUp
import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord, MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._

class MongoFollowUpDAO extends FollowUpDAO {

  def create(followUp: FollowUp) {
    toFollowUpRecord(followUp).save;
  }

  private def toFollowUpRecord(followUp: FollowUp): FollowUpRecord = {

    val commitInfo = FollowUpCommitInfoRecord.createRecord
      .id(followUp.commit.id)
      .message(followUp.commit.message)
      .author(followUp.commit.authorName)
      .date(followUp.commit.date.toDate)

    FollowUpRecord.createRecord
      .commit(commitInfo)
      .user_id(followUp.user.id)
      .status(followUp.status.toString)
  }

}

class FollowUpRecord extends MongoRecord[FollowUpRecord] with ObjectIdPk[FollowUpRecord] {
  def meta = FollowUpRecord

  object user_id extends ObjectIdField(this)
  object commit extends BsonRecordField(this, FollowUpCommitInfoRecord)
  object status extends LongStringField(this)
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
