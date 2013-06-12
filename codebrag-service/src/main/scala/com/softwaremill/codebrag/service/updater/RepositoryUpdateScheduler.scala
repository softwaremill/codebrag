package com.softwaremill.codebrag.service.updater

import akka.actor.{Props, ActorSystem}
import scala.concurrent.duration._
import com.softwaremill.codebrag.service.github.GitHubCommitImportServiceFactory
import com.softwaremill.codebrag.service.config.CodebragConfiguration

object RepositoryUpdateScheduler {

  def initialize(actorSystem: ActorSystem, importServiceFactory: GitHubCommitImportServiceFactory) {

    import actorSystem.dispatcher

    val authorizedLogin = CodebragConfiguration.syncUserLogin
    val importService = importServiceFactory.createInstance(authorizedLogin)
    val updaterActor = actorSystem.actorOf(Props(new LocalRepositoryUpdater("softwaremill", "codebrag", importService)))

    actorSystem.scheduler.schedule(20 seconds,
      45 seconds,
      updaterActor,
      LocalRepositoryUpdater.Tick)
  }
}
