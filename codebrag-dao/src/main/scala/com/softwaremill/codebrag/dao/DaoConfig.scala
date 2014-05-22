package com.softwaremill.codebrag.dao

import com.typesafe.config.Config
import scala.Predef._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.common.config.ConfigWithDefault

trait DaoConfig extends ConfigWithDefault with Logging {
  def rootConfig: Config

  lazy val embeddedDataDir: String = getString("codebrag.data-dir", "./data")
  lazy val embeddedBackupHour: Int = getInt("storage.embedded.backup-hour", 5)
}
