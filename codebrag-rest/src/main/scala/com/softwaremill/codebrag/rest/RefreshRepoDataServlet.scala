package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.commits.{RepoDataProducer, CommitImportService}
import com.softwaremill.codebrag.service.config.CodebragConfig
import scala.sys.process.Process
import org.apache.commons.lang3.exception.ExceptionUtils
import akka.actor.ActorSystem
import com.softwaremill.codebrag.service.updater.{RepositoryUpdateScheduler, LocalRepositoryUpdater}
import com.typesafe.scalalogging.slf4j.Logging

class RefreshRepoDataServlet(repoDataProducer: RepoDataProducer, actorSystem: ActorSystem) extends JsonServlet with Logging {

  get("/") {
    repoDataProducer.createFromConfiguration() match {
      case Some(repoData) => {
        RepositoryUpdateScheduler.actor ! LocalRepositoryUpdater.RefreshRepoData(repoData)
        "Succesfully refreshed repository configuration"
      }
      case None => halt(403, "Could not initialize valid repository configuration")
    }
  }

}

object RefreshRepoDataServlet {
  val MappingPath = "/refresh"
}
