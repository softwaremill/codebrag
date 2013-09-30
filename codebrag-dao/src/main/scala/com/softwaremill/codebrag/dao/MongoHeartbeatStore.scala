package com.softwaremill.codebrag.dao

import org.bson.types.ObjectId
import org.joda.time.{DateTimeZone, DateTime}
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{DateField, ObjectIdPk}
import com.foursquare.rogue.LiftRogue._

trait HeartbeatStore {
  def update(userId: ObjectId)

  def get(userId: ObjectId): Option[DateTime]
}

class MongoHeartbeatStore extends HeartbeatStore {
  def update(userId: ObjectId) {
    val currentDateTime = DateTime.now(DateTimeZone.UTC)
    val query = HeartbeatRecord.where(_.id eqs userId).asDBObject
    val record = HeartbeatRecord.createRecord.lastHeartbeat(currentDateTime.toDate).id(userId).asDBObject
    HeartbeatRecord.upsert(query, record)
  }

  def get(userId: ObjectId): Option[DateTime] = {
    HeartbeatRecord select (_.lastHeartbeat) where (_.id eqs userId) get() map (new DateTime(_))
  }
}

class HeartbeatRecord extends MongoRecord[HeartbeatRecord] with ObjectIdPk[HeartbeatRecord] {
  def meta = HeartbeatRecord

  object lastHeartbeat extends DateField(this)

}

object HeartbeatRecord extends HeartbeatRecord with MongoMetaRecord[HeartbeatRecord] {
  override def collectionName: String = "heartbeats"
}
