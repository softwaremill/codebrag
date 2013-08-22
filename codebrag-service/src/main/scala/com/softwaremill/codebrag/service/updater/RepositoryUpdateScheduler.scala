package com.softwaremill.codebrag.service.updater

import akka.actor.{ActorRef, Props, ActorSystem}
import com.softwaremill.codebrag.service.commits.{RepoDataProducer, CommitImportService}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.updater.LocalRepositoryUpdater.UpdateCommand

object RepositoryUpdateScheduler extends Logging {

  private var actor: ActorRef = _

  def initialize(actorSystem: ActorSystem, repoDataProducer: RepoDataProducer, commitImportService: CommitImportService): ActorRef = {
    actor = actorSystem.actorOf(Props(new LocalRepositoryUpdater(commitImportService, actorSystem)), "repositoryUpdater")
    initializeRepositoryData(repoDataProducer)
    scheduleRepositorySynchronization(actorSystem)
    actor
  }


  def initializeRepositoryData(repoDataProducer: RepoDataProducer) {
    repoDataProducer.createFromConfiguration().foreach {
      repoData => actor ! LocalRepositoryUpdater.RefreshRepoData(repoData)
    }
  }

  def scheduleRepositorySynchronization(actorSystem: ActorSystem) {
    import actorSystem.dispatcher
    actorSystem.scheduler.scheduleOnce(UpdateCommand.InitialDelay, actor, LocalRepositoryUpdater.UpdateCommand)
  }
}
