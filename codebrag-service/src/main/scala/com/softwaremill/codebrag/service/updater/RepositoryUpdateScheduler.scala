package com.softwaremill.codebrag.service.updater

import akka.actor.{Props, ActorSystem}
import scala.concurrent.duration._
import com.softwaremill.codebrag.service.commits.{RepoDataProducer, CommitImportService}
import com.typesafe.scalalogging.slf4j.Logging

object RepositoryUpdateScheduler extends Logging {

  def initialize(actorSystem: ActorSystem,
                 repoDataProducer: RepoDataProducer,
                 commitImportService: CommitImportService) {

    import actorSystem.dispatcher

    repoDataProducer.createFromConfiguration().foreach { repoData =>
      val updaterActor = actorSystem.actorOf(Props(
        new LocalRepositoryUpdater(repoData, commitImportService)))

      actorSystem.scheduler.schedule(60 seconds,
        45 seconds,
        updaterActor,
        LocalRepositoryUpdater.UpdateCommand)
    }
  }
}
