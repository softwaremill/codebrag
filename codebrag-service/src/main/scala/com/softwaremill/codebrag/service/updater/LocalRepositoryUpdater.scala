package com.softwaremill.codebrag.service.updater

import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.commits.{RepoData, CommitImportService}

class LocalRepositoryUpdater(importService: CommitImportService) extends Actor with Logging {

  private var repoData: Option[RepoData] = None

  def receive = {
    case LocalRepositoryUpdater.RefreshRepoData(newRepoData)  => {
      repoData = Some(newRepoData)
      logger.debug("Repository credentials refreshed")
    }
    case LocalRepositoryUpdater.UpdateCommand => {
      repoData.foreach(importService.importRepoCommits)
    }
  }
}

object LocalRepositoryUpdater {
  object UpdateCommand
  case class RefreshRepoData(newRepoData: RepoData)
}