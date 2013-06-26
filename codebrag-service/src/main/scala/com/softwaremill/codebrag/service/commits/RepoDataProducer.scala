package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.service.config.{RepositoryConfig, CodebragConfig}
import com.typesafe.scalalogging.slf4j.Logging
import org.eclipse.jgit.util.StringUtils

class RepoDataProducer(userDao: UserDAO, config: CodebragConfig with RepositoryConfig) extends Logging {

  def createFromConfiguration(): Option[RepoData] = {
    val authorizedLogin = config.codebragSyncUserLogin
    if (StringUtils.isEmptyOrNull(authorizedLogin)) {
      logger.error("Cannot schedule automatic synchronization. Value syncUserLogin not configured in application.conf.")
      None
    } else {
      val token = fetchGitHubToken(authorizedLogin)
      Some(new GitHubRepoData(config.repositoryOwner, config.repositoryName, token))
    }
  }

  private def fetchGitHubToken(login: String) = {
    val importingUserOpt = userDao.findByLoginOrEmail(login)

    val token = importingUserOpt match {
      case Some(user) => user.authentication.token
      case None => {
        logger.warn(s"User $login not found in DB. Cannot properly initialize commit importer")
        s"user-$login-not-found"
      }
    }

    token
  }
}
