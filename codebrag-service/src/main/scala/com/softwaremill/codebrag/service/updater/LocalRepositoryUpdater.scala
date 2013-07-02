package com.softwaremill.codebrag.service.updater

import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.commits.{RepoData, CommitImportService}

class LocalRepositoryUpdater(repoData: RepoData, importService: CommitImportService) extends Actor with Logging {

  def receive = {
    case LocalRepositoryUpdater.UpdateCommand =>
      importService.importRepoCommits(repoData)
  }
}

object LocalRepositoryUpdater {
  val UpdateCommand = "UpdateCommand"
}