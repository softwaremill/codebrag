package com.softwaremill.codebrag.dao.sql

import scala.slick.driver.JdbcProfile
import org.joda.time.{DateTimeZone, DateTime}

case class SQLDatabase(db: scala.slick.jdbc.JdbcBackend.Database, driver: JdbcProfile) {
  import driver.simple._

  implicit val dateTimeColumnType = MappedColumnType.base[DateTime, java.sql.Timestamp](
    dt => new java.sql.Timestamp(dt.getMillis),
    t => new DateTime(t.getTime).withZone(DateTimeZone.UTC)
  )
}
