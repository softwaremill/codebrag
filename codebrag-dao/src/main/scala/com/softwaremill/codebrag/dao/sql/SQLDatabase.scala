package com.softwaremill.codebrag.dao.sql

import scala.slick.driver.JdbcProfile
import org.joda.time.{DateTimeZone, DateTime}
import org.bson.types.ObjectId
import java.io.File
import scala.slick.jdbc.JdbcBackend._
import com.softwaremill.codebrag.dao.{SQLDaos, DaoConfig}
import com.typesafe.scalalogging.slf4j.Logging
import com.googlecode.flyway.core.Flyway

case class SQLDatabase(db: scala.slick.jdbc.JdbcBackend.Database, driver: JdbcProfile) extends Logging {
  import driver.simple._

  implicit val dateTimeColumnType = MappedColumnType.base[DateTime, java.sql.Timestamp](
    dt => new java.sql.Timestamp(dt.getMillis),
    t => new DateTime(t.getTime).withZone(DateTimeZone.UTC)
  )

  implicit val objectIdColumnType = MappedColumnType.base[ObjectId, String](
    oi => oi.toString,
    x => new ObjectId(x)
  )

  def createTablesIfNeeded(daos: SQLDaos) {
    val allSchemas =
      daos.commentDao.schemas ++
        daos.commitInfoDao.schemas ++
        daos.commitReviewTaskDao.schemas ++
        daos.eventDao.schemas ++
        daos.followupDao.schemas ++
        daos.invitationDao.schemas ++
        daos.likeDao.schemas ++
        daos.userDao.schemas ++
        daos.repoStatusDao.schemas ++
        daos.heartbeatDao.schemas

    try {
      db.withTransaction { implicit session =>
        allSchemas.foreach { _.create }
      }

      logger.info("Schemas created")
    } catch {
      case e: Exception => logger.info(s"Cannot create schema because of ${e.getMessage}. This is probably because the " +
        s"schema already exist. We are not concerned in doing it in a nicer way right now because we are shortly " +
        s"moving to FlyWay anyway.")
    }
  }
}

object SQLDatabase extends Logging {
  def connectionString(config: DaoConfig): String = {
    val fullPath = new File(config.embeddedDataDir, "codebrag").getCanonicalPath
    logger.info(s"Using an embedded database, with data files located at: $fullPath")

    s"jdbc:h2:file:$fullPath"
  }

  def createEmbedded(config: DaoConfig) = {
    val db = Database.forURL(connectionString(config), driver="org.h2.Driver")
    SQLDatabase(db, scala.slick.driver.H2Driver)
  }

  def updateSchema(config: DaoConfig) {
    updateSchema(connectionString(config))
  }

  def updateSchema(connectionString: String) {
    val flyway = new Flyway()
    flyway.setDataSource(connectionString, "", "")
    flyway.migrate()
  }
}