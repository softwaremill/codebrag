package com.softwaremill.codebrag.service.updater

import akka.actor.{ActorRef, Props, ActorSystem}
import com.softwaremill.codebrag.service.commits.{DiffLoader, CommitImportService}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.repository.config.RepoData

object RepositoryUpdateScheduler extends Logging {

  private var actor: ActorRef = _

  def initialize(
    actorSystem: ActorSystem,
    repoData: RepoData,
    commitImportService: CommitImportService,
    diffLoader: DiffLoader): ActorRef = {

    actor = actorSystem.actorOf(Props(new RepoActor(commitImportService, diffLoader, repoData)),
      repoData.repoName + "-repo-actor")
    scheduleRepositorySynchronization(actorSystem)
    actor
  }

  def scheduleRepositorySynchronization(actorSystem: ActorSystem) {
    import actorSystem.dispatcher
    actorSystem.scheduler.scheduleOnce(RepoActor.InitialDelay, actor, RepoActor.Update(scheduleNext = true))
  }
}
