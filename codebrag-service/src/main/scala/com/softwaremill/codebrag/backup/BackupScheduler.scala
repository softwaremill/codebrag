package com.softwaremill.codebrag.backup

import com.typesafe.scalalogging.slf4j.Logging
import akka.actor.ActorSystem
import com.softwaremill.codebrag.common.Clock
import com.softwaremill.codebrag.common.scheduling.ScheduleDelaysCalculator
import com.softwaremill.codebrag.dao.DaoConfig
import scala.concurrent.duration._
import com.softwaremill.codebrag.dao.sql.SQLEmbeddedDbBackup

object BackupScheduler extends Logging {
  def initialize(actorSystem: ActorSystem, sqlEmbeddedDbBackup: SQLEmbeddedDbBackup, config: DaoConfig, clock: Clock) {
    import actorSystem.dispatcher

    val initialDelay = ScheduleDelaysCalculator.delayToGivenTimeInMillis(config.embeddedBackupHour, 0)(clock).millis
    actorSystem.scheduler.schedule(initialDelay, 24.hours, new Runnable {
      def run() {
        sqlEmbeddedDbBackup.backup()
      }
    })

    logger.info(s"Scheduled embedded database backups to run every day at ${config.embeddedBackupHour}:00 UTC.")
  }
}
