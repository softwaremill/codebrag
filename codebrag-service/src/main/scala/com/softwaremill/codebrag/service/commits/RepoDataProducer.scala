package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.service.config.RepositoryConfig
import com.typesafe.scalalogging.slf4j.Logging
import org.eclipse.jgit.util.StringUtils

class RepoDataProducer(userDao: UserDAO, config: RepositoryConfig) extends Logging {

  def createFromConfiguration(): Option[RepoData] = {
    config.repositoryType match {
      case "github" => createGithubConfig()
      case "git" => createGitConfig()
      case _ => throw new IllegalArgumentException(s"Unknown repository type: ${config.repositoryType}")
    }
  }

  private def createGitConfig() = {
    Some(new GitRepoData(config.gitRepositoryName, config.gitRepositoryUri,
      config.gitRepositoryUsername, config.gitRepositoryPassword))
  }

  private def createGithubConfig() = {
    val authorizedLogin = config.githubRepositorySyncUserLogin
    if (StringUtils.isEmptyOrNull(authorizedLogin)) {
      logger.error("Cannot schedule automatic synchronization. Value syncUserLogin not configured in application.conf.")
      None
    } else {
      val token = fetchGitHubToken(authorizedLogin)
      Some(new GitHubRepoData(config.githubRepositoryOwner, config.githubRepositoryName, token))
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
