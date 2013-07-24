package com.softwaremill.codebrag.service.updater

import akka.actor.{ActorRef, Props, ActorSystem}
import scala.concurrent.duration._
import com.softwaremill.codebrag.service.commits.{RepoDataProducer, CommitImportService}
import com.typesafe.scalalogging.slf4j.Logging

object RepositoryUpdateScheduler extends Logging {

  var actor: ActorRef = _

  def initialize(actorSystem: ActorSystem,
                 repoDataProducer: RepoDataProducer,
                 commitImportService: CommitImportService) {

    import actorSystem.dispatcher

    repoDataProducer.createFromConfiguration().foreach { repoData =>

      actor = actorSystem.actorOf(Props(
        new LocalRepositoryUpdater(repoData, commitImportService)), "repositoryUpdater")

      actorSystem.scheduler.schedule(3 seconds,
        45 seconds,
        actor,
        LocalRepositoryUpdater.UpdateCommand)
    }
  }
}
