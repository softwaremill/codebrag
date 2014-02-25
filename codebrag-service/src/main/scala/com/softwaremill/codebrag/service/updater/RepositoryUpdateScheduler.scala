package com.softwaremill.codebrag.service.updater

import akka.actor.{ActorRef, Props, ActorSystem}
import com.softwaremill.codebrag.service.commits.CommitImportService
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.repository.config.RepoData

object RepositoryUpdateScheduler extends Logging {

  private var actor: ActorRef = _

  def initialize(actorSystem: ActorSystem, repoData: RepoData, commitImportService: CommitImportService): ActorRef = {
    actor = actorSystem.actorOf(Props(new LocalRepositoryUpdater(commitImportService, repoData)), "repositoryUpdater")
    scheduleRepositorySynchronization(actorSystem)
    actor
  }

  def scheduleRepositorySynchronization(actorSystem: ActorSystem) {
    import actorSystem.dispatcher
    actorSystem.scheduler.scheduleOnce(LocalRepositoryUpdater.InitialDelay, actor, LocalRepositoryUpdater.UpdateCommand(scheduleNext = true))
  }
}
