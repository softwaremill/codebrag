package com.softwaremill.codebrag.rest

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.domain.RepositoryStatus
import com.softwaremill.codebrag.repository.config.RepoConfig
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO

class RepoStatusServlet(val authenticator: Authenticator, repoConfig: RepoConfig, repoStatusDao: RepositoryStatusDAO) extends JsonServletWithAuthentication with Logging {

  get("/") {
    getRepositoryStatus(repoConfig)
  }

  private def getRepositoryStatus(repoConfig: RepoConfig): Map[String, RepositoryStatus] = {
    repoStatusDao.getRepoStatus(repoConfig.repoName) match {
      case Some(status) => Map("repoStatus" -> status)
      case None => {
        logger.debug(s"No status found for ${repoConfig.repoName}, assuming it is first run and repo is being cloned at the moment.")
        Map("repoStatus" -> RepositoryStatus.notReady(repoConfig.repoName))
      }
    }
  }

}

object RepoStatusServlet {
  val Mapping = "repoStatus"
}