package com.softwaremill.codebrag.dao

import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord, MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field.{OptionalIntField, OptionalStringField, EnumNameField}
import net.liftweb.common.Box
import com.foursquare.rogue.LiftRogue._
import net.liftweb.json.JsonDSL._
import org.bson.types.ObjectId


class FollowupRecord extends MongoRecord[FollowupRecord] with ObjectIdPk[FollowupRecord] {

  def meta = FollowupRecord

  object receivingUserId extends ObjectIdField(this)

  object reactions extends MongoListField[FollowupRecord, ObjectId](this)

  object threadId extends BsonRecordField(this, ThreadIdRecord)

  object lastReaction extends BsonRecordField(this, LastReactionRecord)

}

object FollowupRecord extends FollowupRecord with MongoMetaRecord[FollowupRecord] {

  override def collectionName = "follow_ups"

  def ensureIndexes() {
    val receivingUserIdField = receivingUserId.name
    val commitIdField = threadId.subfield(_.commitId).name
    val fileNameField = threadId.subfield(_.fileName).name
    val lineNumberField = threadId.subfield(_.lineNumber).name
    this.ensureIndex(keys = (receivingUserIdField -> 1) ~ (commitIdField -> 1) ~ (fileNameField -> 1) ~ (lineNumberField -> 1), unique = true)
  }

}

class LastReactionRecord extends BsonRecord[LastReactionRecord] {
  def meta = LastReactionRecord

  object reactionId extends ObjectIdField(this)

  object reactionType extends EnumNameField(this, LastReactionRecord.ReactionTypeEnum)

  object reactionAuthorId extends ObjectIdField(this)
}

object LastReactionRecord extends LastReactionRecord with BsonMetaRecord[LastReactionRecord] {

  object ReactionTypeEnum extends Enumeration {
    type ReactionTypeEnum = Value
    val Like, Comment = Value
  }

}

class ThreadIdRecord extends BsonRecord[ThreadIdRecord] {
  def meta = ThreadIdRecord

  object commitId extends ObjectIdField(this)

  object fileName extends OptionalStringField(this, Box(None))

  object lineNumber extends OptionalIntField(this, Box(None))

}

object ThreadIdRecord extends ThreadIdRecord with BsonMetaRecord[ThreadIdRecord]
