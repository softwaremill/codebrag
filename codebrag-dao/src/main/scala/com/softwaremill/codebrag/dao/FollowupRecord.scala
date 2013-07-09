package com.softwaremill.codebrag.dao

import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord, MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field.{OptionalIntField, OptionalStringField, EnumNameField}
import net.liftweb.common.Box
import com.foursquare.rogue.LiftRogue._
import net.liftweb.json.JsonDSL._


class FollowupRecord extends MongoRecord[FollowupRecord] with ObjectIdPk[FollowupRecord] {

  def meta = FollowupRecord

  object receivingUserId extends ObjectIdField(this)

  object reactions extends MongoListField(this)

  object commit extends BsonRecordField(this, FollowupCommitInfoRecord)

  object threadId extends BsonRecordField(this, ThreadIdRecord)

  object lastReaction extends BsonRecordField(this, LastReactionRecord)

}

object FollowupRecord extends FollowupRecord with MongoMetaRecord[FollowupRecord] {

  override def collectionName = "follow_ups_new"

  def ensureIndexes() {
    val commitIdField = threadId.subfield(_.commitId).name
    val fileNameField = threadId.subfield(_.fileName).name
    val lineNumberField = threadId.subfield(_.lineNumber).name
    this.ensureIndex(keys = (commitIdField -> 1) ~ (fileNameField -> 1) ~ (lineNumberField -> 1), unique = true)
  }

}

class LastReactionRecord extends BsonRecord[LastReactionRecord] {
  def meta = LastReactionRecord

  object reactionId extends ObjectIdField(this)

  object date extends DateField(this)

  object authorId extends ObjectIdField(this)

  object authorName extends LongStringField(this)

  object shortMsg extends LongStringField(this)

  object reactionType extends EnumNameField(this, LastReactionRecord.ReactionTypeEnum)
}

object LastReactionRecord extends LastReactionRecord with BsonMetaRecord[LastReactionRecord] {

  object ReactionTypeEnum extends Enumeration {
    type ReactionTypeEnum = Value
    val Like, Comment = Value
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
