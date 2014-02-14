package com.softwaremill.codebrag.dao.sql

import scala.slick.jdbc.StaticQuery
import com.softwaremill.codebrag.dao.DaoConfig
import com.softwaremill.codebrag.common.Clock
import java.io.File
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.common.Timed._

class SQLEmbeddedDbBackup(val database: SQLDatabase, config: DaoConfig, clock: Clock) extends Logging {
  import database._

  def backup() {
    val path = backupFileFullPath()
    logger.info(s"Backing up the embedded sql database to $path ...")
    val (_, took) = timed {
      db.withSession { implicit session =>
        StaticQuery.updateNA(s"BACKUP TO '$path'").execute()
      }
    }
    logger.info(s"Backup done, took ${took}ms")
  }

  private def backupFileFullPath() = {
    val dayOfWeek = clock.nowUtc.getDayOfWeek
    new File(config.embeddedDataDir, s"backup$dayOfWeek.zip").getCanonicalPath
  }
}
