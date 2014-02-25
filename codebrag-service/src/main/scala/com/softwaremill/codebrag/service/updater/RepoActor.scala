package com.softwaremill.codebrag.service.updater

import akka.actor._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.commits.{DiffLoader, CommitImportService}
import com.softwaremill.codebrag.repository.config.RepoData
import RepoActor._

/**
 * Handles all interactions with a given repository, so that all access to the filesystem is done serially.
 */
class RepoActor(
  importService: CommitImportService,
  diffLoader: DiffLoader,
  repoData: RepoData) extends Actor with Logging {
  
  def receive = {
    case Update(scheduleNext) => {
      try {
        importService.importRepoCommits(repoData)
      } finally {
        if (scheduleNext) {
          import context.dispatcher
          logger.debug("Scheduling next update delay to " + NextUpdatesInterval.toString)
          context.system.scheduler.scheduleOnce(NextUpdatesInterval, self, Update(scheduleNext = true))
        }
      }
    }

    case GetDiff(sha) => {
      sender ! diffLoader.loadDiff(sha, repoData)
    }
  }
}

object RepoActor {

  import scala.concurrent.duration._

  val InitialDelay = 3.seconds
  val NextUpdatesInterval = 45.seconds

  case class Update(scheduleNext: Boolean)
  case class GetDiff(sha: String)
}