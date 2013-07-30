package com.softwaremill.codebrag.service.updater

import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.commits.{RepoData, CommitImportService}

class LocalRepositoryUpdater(var repoData: RepoData, importService: CommitImportService) extends Actor with Logging {

  def receive = {
    case msg: LocalRepositoryUpdater.RefreshRepoData => {
      repoData = msg.newRepoData
      logger.debug("Repository credentials refreshed")
    }
    case LocalRepositoryUpdater.UpdateCommand => {
      importService.importRepoCommits(repoData)
    }
  }
}

object LocalRepositoryUpdater {
  object UpdateCommand
  case class RefreshRepoData(newRepoData: RepoData)
}