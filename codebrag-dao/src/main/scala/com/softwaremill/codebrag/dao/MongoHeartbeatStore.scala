package com.softwaremill.codebrag.dao

import org.bson.types.ObjectId
import org.joda.time.DateTime
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{DateField, ObjectIdPk}
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.common.Clock

trait HeartbeatStore {
  def update(userId: ObjectId)

  def get(userId: ObjectId): Option[DateTime]

  def loadAll(): List[(ObjectId, DateTime)]
}

class MongoHeartbeatStore(clock: Clock) extends HeartbeatStore {
  def update(userId: ObjectId) {
    val currentDateTime = clock.nowUtc
    val query = HeartbeatRecord.where(_.id eqs userId).asDBObject
    val record = HeartbeatRecord.createRecord.lastHeartbeat(currentDateTime.toDate).id(userId).asDBObject
    HeartbeatRecord.upsert(query, record)
  }

  def get(userId: ObjectId): Option[DateTime] = {
    HeartbeatRecord select (_.lastHeartbeat) where (_.id eqs userId) get() map (new DateTime(_))
  }

  def loadAll(): List[(ObjectId, DateTime)] = {
    HeartbeatRecord.findAll.map(heartbeat => (heartbeat.id.get, new DateTime(heartbeat.lastHeartbeat.get)))
  }
}

class HeartbeatRecord extends MongoRecord[HeartbeatRecord] with ObjectIdPk[HeartbeatRecord] {
  def meta = HeartbeatRecord

  object lastHeartbeat extends DateField(this)

}

object HeartbeatRecord extends HeartbeatRecord with MongoMetaRecord[HeartbeatRecord] {
  override def collectionName: String = "heartbeats"
}