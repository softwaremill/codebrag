package com.softwaremill.codebrag.service.updater

import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.github.GitHubCommitImportService

class LocalRepositoryUpdater(owner: String, repositoryName: String, importService: GitHubCommitImportService) extends Actor with Logging {

  def receive = {
    case LocalRepositoryUpdater.UpdateCommand =>
      importService.importRepoCommits(owner, repositoryName)
  }
}

object LocalRepositoryUpdater {
  val UpdateCommand = "UpdateCommand"
}