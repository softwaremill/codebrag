package com.softwaremill.codebrag.dao.events

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.common.StatisticEvent
import com.softwaremill.codebrag.dao.mongo.LongStringField
import org.joda.time.DateTime
import com.foursquare.rogue.LiftRogue._

class EventDAO extends Logging {
  def storeEvent(event: StatisticEvent) {
    EventRecord.createRecord.date(event.timestamp.toDate).eventType(event.eventType).originatingUserId(event.userId).save
    logger.debug(s"Stored event $event")
  }

  def countEvents(start: DateTime, end: DateTime, eventType: String): Int = {
    val query = EventRecord.where(_.date between(start, end)).and(_.eventType eqs eventType)
    query.count().toInt
  }

  def countActiveUsers(start: DateTime, end: DateTime): Int = {
    EventRecord
      .select(_.originatingUserId)
      .where(_.date between(start, end)).and(_.eventType neqs NewUserRegistered.EventType)
      .fetch()
      .toSet
      .size
  }
}

class EventRecord extends MongoRecord[EventRecord] with ObjectIdPk[EventRecord] {
  def meta = EventRecord

  object date extends DateField(this)

  object eventType extends LongStringField(this)

  object originatingUserId extends ObjectIdField(this)

}

object EventRecord extends EventRecord with MongoMetaRecord[EventRecord] {

  override def collectionName = "events"

}