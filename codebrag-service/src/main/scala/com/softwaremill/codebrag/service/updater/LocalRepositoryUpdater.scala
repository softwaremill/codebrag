package com.softwaremill.codebrag.service.updater

import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.github.{RepoData, GitHubCommitImportService}

class LocalRepositoryUpdater(repoData: RepoData, importService: GitHubCommitImportService) extends Actor with Logging {

  def receive = {
    case LocalRepositoryUpdater.UpdateCommand =>
      importService.importRepoCommits(repoData)
  }
}

object LocalRepositoryUpdater {
  val UpdateCommand = "UpdateCommand"
}