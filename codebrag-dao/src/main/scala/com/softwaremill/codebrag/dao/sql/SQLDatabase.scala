package com.softwaremill.codebrag.dao.sql

import scala.slick.driver.JdbcProfile
import org.joda.time.{DateTimeZone, DateTime}
import org.bson.types.ObjectId
import java.io.File
import scala.slick.jdbc.JdbcBackend._
import com.softwaremill.codebrag.dao.DaoConfig
import com.typesafe.scalalogging.slf4j.Logging

case class SQLDatabase(db: scala.slick.jdbc.JdbcBackend.Database, driver: JdbcProfile) {
  import driver.simple._

  implicit val dateTimeColumnType = MappedColumnType.base[DateTime, java.sql.Timestamp](
    dt => new java.sql.Timestamp(dt.getMillis),
    t => new DateTime(t.getTime).withZone(DateTimeZone.UTC)
  )

  implicit val objectIdColumnType = MappedColumnType.base[ObjectId, String](
    oi => oi.toString,
    x => new ObjectId(x)
  )
}

object SQLDatabase extends Logging {
  def createEmbedded(config: DaoConfig) = {
    val fullPath = new File(config.embeddedDataDir).getAbsolutePath
    logger.info(s"Using an embedded database, with data files located at: $fullPath")

    val connectionString = s"jdbc:h2:$fullPath;DB_CLOSE_DELAY=-1"
    val db = Database.forURL(connectionString, driver="org.h2.Driver")
    SQLDatabase(db, scala.slick.driver.H2Driver)
  }
}