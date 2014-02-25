package com.softwaremill.codebrag.service.updater

import akka.actor._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.commits.CommitImportService
import com.softwaremill.codebrag.repository.config.RepoData

class LocalRepositoryUpdater(importService: CommitImportService, repoData: RepoData, actorSystem: ActorSystem) extends Actor with Logging {

  def receive = {
    case LocalRepositoryUpdater.UpdateCommand(schedule) => {
      try {
        importService.importRepoCommits(repoData)
      } finally {
        if (schedule) {
          import actorSystem.dispatcher
          logger.debug("Scheduling next update delay to " + LocalRepositoryUpdater.NextUpdatesInterval.toString)
          actorSystem.scheduler.scheduleOnce(LocalRepositoryUpdater.NextUpdatesInterval, self, LocalRepositoryUpdater.UpdateCommand(scheduleNext = true))
        }
      }
    }
  }
}

object LocalRepositoryUpdater {

  import scala.concurrent.duration._

  val InitialDelay = 3.seconds
  val NextUpdatesInterval = 45.seconds

  case class UpdateCommand(scheduleNext: Boolean)

}