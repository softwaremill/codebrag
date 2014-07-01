package com.softwaremill.codebrag.service.updater

import akka.actor._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.commits.CommitImportService
import RepoUpdateActor._
import com.softwaremill.codebrag.repository.Repository

class RepoUpdateActor(importService: CommitImportService, repository: Repository) extends Actor with Logging {

  def receive = {
    case Update(scheduleNext) => {    // TODO: get rid of this scheduleNext
      try {
        importService.importRepoCommits(repository)
        importService.cleanupStaleBranches(repository)
      } finally {
        if (scheduleNext) {
          import context.dispatcher
          logger.debug(s"Scheduling next update of ${repository.repoName} in ${NextUpdatesInterval.toString}")
          context.system.scheduler.scheduleOnce(NextUpdatesInterval, self, Update(scheduleNext = true))
        }
      }
    }
  }
}

object RepoUpdateActor {

  import scala.concurrent.duration._

  val InitialDelay = 3.seconds
  val NextUpdatesInterval = 45.seconds

  case class Update(scheduleNext: Boolean)
}