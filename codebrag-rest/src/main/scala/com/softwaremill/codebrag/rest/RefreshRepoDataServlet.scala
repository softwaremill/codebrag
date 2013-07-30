package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.commits.RepoDataProducer
import akka.actor.ActorRef
import com.softwaremill.codebrag.service.updater.LocalRepositoryUpdater
import com.typesafe.scalalogging.slf4j.Logging

class RefreshRepoDataServlet(repoDataProducer: RepoDataProducer, repositoryUpdateActor: ActorRef) extends JsonServlet with Logging {

  get("/") {
    repoDataProducer.createFromConfiguration() match {
      case Some(repoData) => {
        repositoryUpdateActor ! LocalRepositoryUpdater.RefreshRepoData(repoData)
        "Succesfully refreshed repository configuration"
      }
      case None => halt(403, "Could not initialize valid repository configuration")
    }
  }

}

object RefreshRepoDataServlet {
  val MappingPath = "/refresh"
}
