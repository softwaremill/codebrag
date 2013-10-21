package com.softwaremill.codebrag.rest

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.config.{RepositoryConfig, CodebragConfig}
import com.softwaremill.codebrag.dao.{RepositoryStatusDAO, UserDAO}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.service.commits.RepoDataProducer
import org.scalatra.BadRequest
import com.softwaremill.codebrag.domain.RepositoryStatus

class RepoStatusServlet(val authenticator: Authenticator, repoDataProducer: RepoDataProducer, repoStatusDao: RepositoryStatusDAO) extends JsonServletWithAuthentication with Logging {

  get("/") {
    repoDataProducer.createFromConfiguration() match {
      case Some(repoData) => {
        repoStatusDao.getRepoStatus(repoData.repositoryName) match {
          case Some(status) => Map("repoStatus" -> status)
          case None => Map("repoStatus" -> RepositoryStatus.notReady(repoData.repositoryName))
        }
      }
      case None => BadRequest("Cannot get repository information from configuration")
    }
  }

}

object RepoStatusServlet {
  val Mapping = "repoStatus"
}