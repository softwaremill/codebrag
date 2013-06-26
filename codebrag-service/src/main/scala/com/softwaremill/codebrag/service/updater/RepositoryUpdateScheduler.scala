package com.softwaremill.codebrag.service.updater

import akka.actor.{Props, ActorSystem}
import scala.concurrent.duration._
import com.softwaremill.codebrag.service.commits.{CommitImportService, GitHubRepoData, GitHubCommitImportServiceFactory}
import com.softwaremill.codebrag.service.config.{CodebragConfig, RepositoryConfig}
import org.eclipse.jgit.util.StringUtils
import com.typesafe.scalalogging.slf4j.Logging

object RepositoryUpdateScheduler extends Logging {

  def initialize(actorSystem: ActorSystem,
                 importServiceFactory: GitHubCommitImportServiceFactory,
                 commitImportService: CommitImportService,
                 config: RepositoryConfig with CodebragConfig) {

    val authorizedLogin = config.codebragSyncUserLogin
    if (StringUtils.isEmptyOrNull(authorizedLogin)) {
      logger.error("Cannot schedule automatic synchronization. Value syncUserLogin not configured in application.conf.")
    }
    else {
      scheduleUpdate(commitImportService, importServiceFactory, authorizedLogin, actorSystem, config)
    }
  }

  private def scheduleUpdate(commitImportService: CommitImportService, importServiceFactory: GitHubCommitImportServiceFactory, authorizedLogin: String, actorSystem: ActorSystem, repositoryConfig: RepositoryConfig) {

    import actorSystem.dispatcher

    val updaterActor = actorSystem.actorOf(Props(new LocalRepositoryUpdater(
      new GitHubRepoData(repositoryConfig.repositoryOwner, repositoryConfig.repositoryName,
        importServiceFactory.fetchToken(authorizedLogin)),
      commitImportService)))

    actorSystem.scheduler.schedule(60 seconds,
      45 seconds,
      updaterActor,
      LocalRepositoryUpdater.UpdateCommand)
  }
}
