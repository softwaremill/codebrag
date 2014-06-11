package com.softwaremill.codebrag.service.updater

import akka.actor.{Props, ActorSystem}
import com.softwaremill.codebrag.service.commits.CommitImportService
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.repository.Repository

object RepositoryUpdateScheduler extends Logging {

  def scheduleUpdates(actorSystem: ActorSystem, repositories: Seq[Repository], commitImportService: CommitImportService) {
    repositories.foreach(scheduleSingleRepoUpdate(actorSystem, _, commitImportService))
  }

  private def scheduleSingleRepoUpdate(actorSystem: ActorSystem, repository: Repository, commitImportService: CommitImportService) {
    val props = Props(new RepoUpdateActor(commitImportService, repository))
    val actorName = s"${repository.repoName}-repo-update-actor"
    val actor = actorSystem.actorOf(props, actorName)
    import actorSystem.dispatcher
    actorSystem.scheduler.scheduleOnce(RepoUpdateActor.InitialDelay, actor, RepoUpdateActor.Update(scheduleNext = true))
  }

}
