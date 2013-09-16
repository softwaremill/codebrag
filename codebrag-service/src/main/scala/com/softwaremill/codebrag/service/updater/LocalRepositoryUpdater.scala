package com.softwaremill.codebrag.service.updater

import akka.actor._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.commits.{RepoData, CommitImportService}
import scala.Some
import com.softwaremill.codebrag.service.updater.LocalRepositoryUpdater.{RepositoryUpdateFailed, UpdateCommand}

class LocalRepositoryUpdater(importService: CommitImportService, actorSystem: ActorSystem) extends Actor with Logging {

  private var repoData: Option[RepoData] = None

  def receive = {
    case LocalRepositoryUpdater.RefreshRepoData(newRepoData) => {
      repoData = Some(newRepoData)
      logger.debug("Repository credentials refreshed")
    }
    case LocalRepositoryUpdater.UpdateCommand => {
      try {
        repoData.foreach(importService.importRepoCommits)
        sender ! LocalRepositoryUpdater.RepositoryUpdateSucceeded("Repository synchronized")
      } catch {
        case e: Exception => {
          logger.error("Exception while importing commits", e)
          sender ! RepositoryUpdateFailed(e)
        }
      } finally {
        import actorSystem.dispatcher
        logger.debug("Scheduling next update delay to " + UpdateCommand.NextUpdatesInterval.toString)
        actorSystem.scheduler.scheduleOnce(UpdateCommand.NextUpdatesInterval, self, LocalRepositoryUpdater.UpdateCommand)
      }
    }
  }
}

object LocalRepositoryUpdater {

  object UpdateCommand {
    import scala.concurrent.duration._
    val InitialDelay = 3 seconds
    val NextUpdatesInterval = 45 seconds
  }
  case class RefreshRepoData(newRepoData: RepoData)

  case class RepositoryUpdateSucceeded(message: String)

  case class RepositoryUpdateFailed(reason: Exception)

}