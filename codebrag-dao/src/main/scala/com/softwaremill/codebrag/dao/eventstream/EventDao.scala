package com.softwaremill.codebrag.dao.eventstream

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.common.StatisticEvent
import com.softwaremill.codebrag.dao.mongo.LongStringField


class EventDao extends Logging {

  def storeEvent(event: StatisticEvent) {
    EventRecord.createRecord.date(event.timestamp.toDate).eventType(event.eventType).originatingUserId(event.userId).save
    logger.debug(s"Stored event $event")
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