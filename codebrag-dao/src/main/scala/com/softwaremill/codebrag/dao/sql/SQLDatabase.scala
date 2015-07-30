package com.softwaremill.codebrag.dao.sql

import java.io.File
import javax.sql.DataSource

import com.googlecode.flyway.core.Flyway
import com.mchange.v2.c3p0.{ComboPooledDataSource, DataSources}
import com.softwaremill.codebrag.dao.DaoConfig
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import org.joda.time.{DateTime, DateTimeZone}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend._

case class SQLDatabase(
  db: scala.slick.jdbc.JdbcBackend.Database,
  driver: JdbcProfile,
  ds: DataSource) extends Logging {

  import driver.simple._

  implicit val dateTimeColumnType = MappedColumnType.base[DateTime, java.sql.Timestamp](
    dt => new java.sql.Timestamp(dt.getMillis),
    t => new DateTime(t.getTime).withZone(DateTimeZone.UTC)
  )

  implicit val objectIdColumnType = MappedColumnType.base[ObjectId, String](
    oi => oi.toString,
    x => new ObjectId(x)
  )

  def updateSchema() {
    val flyway = new Flyway()
    flyway.setDataSource(ds)
    flyway.migrate()
  }

  def close() {
    DataSources.destroy(ds)
  }
}

object SQLDatabase extends Logging {
  def connectionString(config: DaoConfig): String = {
    val fullPath = new File(config.embeddedDataDir, "codebrag").getCanonicalPath
    logger.info(s"Using an embedded database, with data files located at: $fullPath")

    s"jdbc:h2:file:$fullPath;AUTO_SERVER=TRUE"
  }

  def createEmbedded(config: DaoConfig): SQLDatabase = {
    createEmbedded(connectionString(config))
  }

  def createEmbedded(connectionString: String): SQLDatabase = {
    val ds = createConnectionPool(connectionString)
    val db = Database.forDataSource(ds)
    SQLDatabase(db, scala.slick.driver.H2Driver, ds)
  }

  private def createConnectionPool(connectionString: String) = {
    val cpds = new ComboPooledDataSource()
    cpds.setDriverClass("org.h2.Driver")
    cpds.setJdbcUrl(connectionString)
    cpds
  }
}