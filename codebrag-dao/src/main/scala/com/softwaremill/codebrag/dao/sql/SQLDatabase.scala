package com.softwaremill.codebrag.dao.sql

import scala.slick.driver.JdbcProfile

case class SQLDatabase(db: scala.slick.jdbc.JdbcBackend.Database, driver: JdbcProfile)
