package com.softwaremill.codebrag.migration

import com.softwaremill.codebrag.common.RealTimeClock
import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.dao.{Daos, DaoConfig}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.Logging

trait MigrationConfig extends Logging with DaoConfig {

  def rootConfig = ConfigFactory.load()

  val daos = new Daos {
    val sqlDatabase = SQLDatabase.createEmbedded(MigrationConfig.this)
    val clock = RealTimeClock
  }
}
