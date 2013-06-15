package com.softwaremill.codebrag.service.updater

import akka.actor.{Props, ActorSystem}
import scala.concurrent.duration._
import com.softwaremill.codebrag.service.github.GitHubCommitImportServiceFactory
import com.softwaremill.codebrag.service.config.{RepositoryConfig, CodebragConfiguration}

object RepositoryUpdateScheduler {

  def initialize(actorSystem: ActorSystem,
                 importServiceFactory: GitHubCommitImportServiceFactory,
                 repositoryConfig: RepositoryConfig) {

    import actorSystem.dispatcher

    val authorizedLogin = CodebragConfiguration.syncUserLogin
    val importService = importServiceFactory.createInstance(authorizedLogin)
    val updaterActor = actorSystem.actorOf(Props(new LocalRepositoryUpdater(repositoryConfig.repositoryOwner,
      repositoryConfig.repositoryName, importService)))

    actorSystem.scheduler.schedule(60 seconds,
      45 seconds,
      updaterActor,
      LocalRepositoryUpdater.Tick)
  }
}
