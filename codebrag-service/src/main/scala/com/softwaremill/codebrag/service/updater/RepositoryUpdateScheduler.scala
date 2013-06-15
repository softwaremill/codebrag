package com.softwaremill.codebrag.service.updater

import akka.actor.{Props, ActorSystem}
import scala.concurrent.duration._
import com.softwaremill.codebrag.service.github.GitHubCommitImportServiceFactory
import com.softwaremill.codebrag.service.config.{CodebragConfig, RepositoryConfig}
import org.eclipse.jgit.util.StringUtils
import com.typesafe.scalalogging.slf4j.Logging

object RepositoryUpdateScheduler extends Logging {

  def initialize(actorSystem: ActorSystem,
                 importServiceFactory: GitHubCommitImportServiceFactory,
                 repositoryConfig: RepositoryConfig,
                 codebragConfiguration: CodebragConfig) {

    val authorizedLogin = codebragConfiguration.syncUserLogin
    if (StringUtils.isEmptyOrNull(authorizedLogin)) {
      logger.error("Cannot schedule automatic synchronization. Value syncUserLogin not configured in application.conf.")
    }
    else {
      scheduleUpdate(importServiceFactory, authorizedLogin, actorSystem, repositoryConfig)
    }
  }

  private def scheduleUpdate(importServiceFactory: GitHubCommitImportServiceFactory, authorizedLogin: String, actorSystem: ActorSystem, repositoryConfig: RepositoryConfig) {

    import actorSystem.dispatcher

    val importService = importServiceFactory.createInstance(authorizedLogin)
    val updaterActor = actorSystem.actorOf(Props(new LocalRepositoryUpdater(repositoryConfig.repositoryOwner,
      repositoryConfig.repositoryName, importService)))

    actorSystem.scheduler.schedule(60 seconds,
      45 seconds,
      updaterActor,
      LocalRepositoryUpdater.Tick)
  }
}
