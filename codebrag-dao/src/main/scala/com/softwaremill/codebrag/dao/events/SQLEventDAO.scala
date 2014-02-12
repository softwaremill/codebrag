package com.softwaremill.codebrag.dao.events

import com.softwaremill.codebrag.dao.sql.{WithSQLSchemas, SQLDatabase}
import org.joda.time.DateTime
import com.softwaremill.codebrag.common.StatisticEvent
import scala.slick.driver.JdbcProfile
import org.bson.types.ObjectId

class SQLEventDAO(database: SQLDatabase) extends EventDAO with WithSQLSchemas {
  import database.driver.simple._
  import database._

  def storeEvent(event: StatisticEvent) {
    db.withTransaction { implicit session =>
      events += SQLEvent(new ObjectId(), event.timestamp, event.eventType, event.userId)
    }
  }

  def countEvents(start: DateTime, end: DateTime, eventType: String) = db.withTransaction { implicit session =>
    val q = events.filter(e => e.date >= start && e.date <= end && e.eventType === eventType).length
    Query(q).first()
  }

  def countActiveUsers(start: DateTime, end: DateTime) = db.withTransaction { implicit session =>
    events
      .filter(e => e.date >= start && e.date <= end && e.eventType =!= NewUserRegistered.EventType)
      .map(_.originatingUserId)
      .list().toSet.size
  }

  case class SQLEvent(id: ObjectId, date: DateTime, eventType: String, originatingUserId: ObjectId)

  private class Events(tag: Tag) extends Table[SQLEvent](tag, "events") {
    def id                = column[ObjectId]("id", O.PrimaryKey)
    def date              = column[DateTime]("event_date")
    def eventType         = column[String]("event_type")
    def originatingUserId = column[ObjectId]("originating_user_id")

    def * = (id, date, eventType, originatingUserId) <> (SQLEvent.tupled, SQLEvent.unapply)
  }

  private val events = TableQuery[Events]

  def schemas: Iterable[JdbcProfile#DDLInvoker] = List(events.ddl)
}
