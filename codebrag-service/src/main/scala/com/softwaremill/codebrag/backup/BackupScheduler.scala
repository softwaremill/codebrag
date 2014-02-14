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
    val scheduledBackups = actorSystem.scheduler.schedule(initialDelay, 24.hours, new Runnable {
      def run() {
        sqlEmbeddedDbBackup.backup()
      }
    })

    // On termination, Akka tries to run all scheduled tasks. We don't want that to happen for backups, as the
    // backup may take a while, is interrupted by the shutdown process, and we end up with a corrupt backup file.
    actorSystem.registerOnTermination(scheduledBackups.cancel())

    logger.info(s"Scheduled embedded database backups to run every day at ${config.embeddedBackupHour}:00 UTC.")
  }
}
