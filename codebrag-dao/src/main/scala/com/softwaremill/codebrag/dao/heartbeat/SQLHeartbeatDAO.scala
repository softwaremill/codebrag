package com.softwaremill.codebrag.dao.heartbeat

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.Clock
import org.joda.time.DateTime

class SQLHeartbeatDAO(database: SQLDatabase, clock: Clock) extends HeartbeatDAO {
  import database.driver.simple._
  import database._

  def update(userId: ObjectId) {
    db.withTransaction { implicit session =>
      heartbeats.filter(_.userId === userId).firstOption match {
        case Some(h) => heartbeats.filter(_.userId === userId).map(_.lastHeartbeat).update(clock.nowUtc)
        case None => heartbeats += SQLHeartbeat(userId, clock.nowUtc)
      }
    }
  }

  def get(userId: ObjectId) = db.withTransaction { implicit session =>
    heartbeats.filter(_.userId === userId).map(_.lastHeartbeat).firstOption
  }

  def loadAll() = db.withTransaction { implicit session =>
    heartbeats.list().map(h => (h.userId, h.lastHeartbeat))
  }

  private case class SQLHeartbeat(userId: ObjectId, lastHeartbeat: DateTime)

  private class Heartbeats(tag: Tag) extends Table[SQLHeartbeat](tag, "heartbeats") {
    def userId        = column[ObjectId]("user_id", O.PrimaryKey)
    def lastHeartbeat = column[DateTime]("last_heartbeat")

    def * = (userId, lastHeartbeat) <> (SQLHeartbeat.tupled, SQLHeartbeat.unapply)
  }

  private val heartbeats = TableQuery[Heartbeats]
}
