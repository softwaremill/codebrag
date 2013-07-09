package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{ThreadDetails, Followup}
import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord, MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import com.foursquare.rogue.LiftRogue._
import org.bson.types.ObjectId
import net.liftweb.record.field.{EnumNameField, EnumField, OptionalIntField, OptionalStringField}
import org.joda.time.DateTime
import scala.None
import net.liftweb.common.Box
import net.liftweb.json.JsonDSL._
import com.foursquare.rogue.Query
import com.foursquare.rogue

class MongoFollowupDAO extends FollowupDAO {


  def findById(followupId: ObjectId): Option[Followup] = {
    FollowupRecord.where(_.followupId eqs followupId).get() match {
      case Some(record) => Some(toFollowup(record))
      case None => None
    }
  }

  def createOrUpdateExisting(followup: Followup) = {
    val alreadyExistsQuery = FollowupRecord
      .where(_.user_id eqs followup.userId)
      .and(_.threadId.subselect(_.commitId) eqs followup.threadId.commitId)
      .and(_.threadId.subselect(_.fileName) exists(followup.threadId.fileName.isDefined))
      .andOpt(followup.threadId.fileName)(_.threadId.subselect(_.fileName) eqs _)
      .and(_.threadId.subselect(_.lineNumber) exists(followup.threadId.lineNumber.isDefined))
      .andOpt(followup.threadId.lineNumber)(_.threadId.subselect(_.lineNumber) eqs _)

    val modificationQuery = buildModificationQuery(followup, alreadyExistsQuery)
    modificationQuery.updateOne(true) match {
      case Some(updated) => {
        updated.followupId.get
      }
      case None => {
        val incomingFollowupRecord = toRecord(followup)
        incomingFollowupRecord.save.followupId.get
      }
    }
  }

  def buildModificationQuery(followup: Followup, query: Query[FollowupRecord, FollowupRecord, rogue.InitialState]) = {
    query.findAndModify(_.author_id setTo followup.authorId)
      .and(_.followupType setTo FollowupRecord.FollowupTypeEnum(followup.followupType.id))
      .and(_.lastCommenterName setTo followup.lastCommenterName)
      .and(_.reactionId setTo followup.reactionId)
      .and(_.date setTo followup.date)
  }

  override def delete(followupId: ObjectId) {
    FollowupRecord.where(_.followupId eqs followupId).findAndDeleteOne()
  }

  private def toFollowup(record: FollowupRecord) = {
    val threadId = ThreadDetails(record.threadId.get.commitId.get, record.threadId.get.lineNumber.get, record.threadId.get.fileName.get)
    val followupType = Followup.FollowupType.apply(record.followupType.get.id)
    Followup(record.reactionId.get, record.author_id.get, record.user_id.get, new DateTime(record.date.get), record.lastCommenterName.get, threadId, followupType)
  }

  private def toRecord(followup: Followup): FollowupRecord = {

    val commitRecord = CommitInfoRecord.where(_.id eqs followup.threadId.commitId).get().getOrElse(
      throw new IllegalStateException(s"Cannot find commit ${followup.threadId.commitId}")
    )

    val threadId = (followup.threadId.fileName, followup.threadId.lineNumber) match {
      case (Some(fileName), Some(lineNumber)) => ThreadIdRecord.createRecord.commitId(followup.threadId.commitId).fileName(fileName).lineNumber(lineNumber)
      case _ => ThreadIdRecord.createRecord.commitId(followup.threadId.commitId)
    }

    val commitInfo = FollowupCommitInfoRecord.createRecord
      .id(followup.threadId.commitId)
      .message(commitRecord.message.get)
      .author(commitRecord.authorName.get)
      .date(commitRecord.committerDate.get)

    FollowupRecord.createRecord
      .followupId(new ObjectId)
      .reactionId(followup.reactionId)
      .commit(commitInfo)
      .author_id(followup.authorId)
      .user_id(followup.userId)
      .date(followup.date.toDate)
      .threadId(threadId)
      .lastCommenterName(followup.lastCommenterName)
      .followupType(FollowupRecord.FollowupTypeEnum.apply(followup.followupType.id))
  }

}

class FollowupRecord extends MongoRecord[FollowupRecord] with ObjectIdPk[FollowupRecord] {
  def meta = FollowupRecord

  object followupId extends ObjectIdField(this)

  object author_id extends ObjectIdField(this)

  object user_id extends ObjectIdField(this)

  object commit extends BsonRecordField(this, FollowupCommitInfoRecord)

  object threadId extends BsonRecordField(this, ThreadIdRecord)

  object date extends DateField(this)

  object reactionId extends ObjectIdField(this)

  object lastCommenterName extends LongStringField(this)

  object followupType extends EnumNameField(this, FollowupRecord.FollowupTypeEnum)

}

object FollowupRecord extends FollowupRecord with MongoMetaRecord[FollowupRecord] {

  override def collectionName = "follow_ups"

  object FollowupTypeEnum extends Enumeration {
    type FollowupType = Value
    val Like, Comment = Value
  }

  def ensureIndexes() {
    val commitIdField = threadId.subfield(_.commitId).name
    val fileNameField = threadId.subfield(_.fileName).name
    val lineNumberField = threadId.subfield(_.lineNumber).name
    this.ensureIndex(keys = (commitIdField -> 1) ~ (fileNameField -> 1) ~ (lineNumberField -> 1), unique = true)
  }

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
