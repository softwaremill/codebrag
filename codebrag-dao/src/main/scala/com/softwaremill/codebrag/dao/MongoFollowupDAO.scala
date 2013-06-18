package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{ThreadDetails, Followup}
import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord, MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import com.foursquare.rogue.LiftRogue._
import org.bson.types.ObjectId
import net.liftweb.record.field.{OptionalIntField, OptionalStringField}
import org.joda.time.DateTime
import scala.None
import net.liftweb.common.Box

class MongoFollowupDAO extends FollowupDAO {


  def findById(followupId: ObjectId): Option[Followup] = {
    FollowupRecord.where(_.followupId eqs followupId).get() match {
      case Some(record) => Some(toFollowup(record))
      case None => None
    }
  }

  def createOrUpdateExisting(followup: Followup) {
    val query = FollowupRecord
      .where(_.user_id eqs followup.userId)
      .and(_.threadId.subselect(_.commitId) eqs followup.threadId.commitId)
      .and(_.threadId.subselect(_.fileName) exists(followup.threadId.fileName.isDefined))
      .andOpt(followup.threadId.fileName)(_.threadId.subselect(_.fileName) eqs _)
      .and(_.threadId.subselect(_.lineNumber) exists(followup.threadId.lineNumber.isDefined))
      .andOpt(followup.threadId.lineNumber)(_.threadId.subselect(_.lineNumber) eqs _)
      .asDBObject
    val commitRecord = CommitInfoRecord.where(_.id eqs followup.threadId.commitId).get().getOrElse(
      throw new IllegalStateException(s"Cannot find commit ${followup.threadId.commitId}")
    )
    FollowupRecord.upsert(query, followupToRecord(followup, commitRecord))
  }

  override def delete(followupId: ObjectId) {
    FollowupRecord.where(_.followupId eqs followupId).findAndDeleteOne()
  }

  private def followupToRecord(followup: Followup, commitRecord: CommitInfoRecord) = {
    val record = toRecord(followup, commitRecord).asDBObject
    record.removeField("_id") // remove _id field, otherwise Mongo screams it cannot modify _id when updating record
    record
  }

  private def toFollowup(record: FollowupRecord) = {
    val threadId = ThreadDetails(record.threadId.get.commitId.get, record.threadId.get.lineNumber.get, record.threadId.get.fileName.get)
    Followup(record.followupId.get, record.reactionId.get, record.user_id.get, new DateTime(record.date.get), record.lastCommenterName.get, threadId)
  }

  private def toRecord(followup: Followup, commitRecord: CommitInfoRecord): FollowupRecord = {

    val commitInfo = FollowupCommitInfoRecord.createRecord
      .id(followup.threadId.commitId)
      .message(commitRecord.message.get)
      .author(commitRecord.authorName.get)
      .date(commitRecord.committerDate.get)

    val threadId = (followup.threadId.fileName, followup.threadId.lineNumber) match {
      case (Some(fileName), Some(lineNumber)) => ThreadIdRecord.createRecord.commitId(followup.threadId.commitId).fileName(fileName).lineNumber(lineNumber)
      case _ => ThreadIdRecord.createRecord.commitId(followup.threadId.commitId)
    }

    FollowupRecord.createRecord
      .followupId(followup.id.getOrElse(new ObjectId))
      .reactionId(followup.reactionId)
      .commit(commitInfo)
      .user_id(followup.userId)
      .date(followup.date.toDate)
      .threadId(threadId)
      .lastCommenterName(followup.lastCommenterName)
  }

}

class FollowupRecord extends MongoRecord[FollowupRecord] with ObjectIdPk[FollowupRecord] {
  def meta = FollowupRecord

  object followupId extends ObjectIdField(this)

  object user_id extends ObjectIdField(this)

  object commit extends BsonRecordField(this, FollowupCommitInfoRecord)

  object threadId extends BsonRecordField(this, ThreadIdRecord)

  object date extends DateField(this)

  object reactionId extends ObjectIdField(this)

  object lastCommenterName extends LongStringField(this)

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


class ThreadIdRecord extends BsonRecord[ThreadIdRecord] {
  def meta = ThreadIdRecord

  object commitId extends ObjectIdField(this)

  object fileName extends OptionalStringField(this, Box(None))

  object lineNumber extends OptionalIntField(this, Box(None))

}

object ThreadIdRecord extends ThreadIdRecord with BsonMetaRecord[ThreadIdRecord]
