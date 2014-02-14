package com.softwaremill.codebrag.rest

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.domain.RepositoryStatus
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO
import com.softwaremill.codebrag.repository.config.RepoData

class RepoStatusServlet(val authenticator: Authenticator, repoData: RepoData, repoStatusDao: RepositoryStatusDAO) extends JsonServletWithAuthentication with Logging {

  get("/") {
    getRepositoryStatus(repoData)
  }

  private def getRepositoryStatus(repoConfig: RepoData): Map[String, RepositoryStatus] = {
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