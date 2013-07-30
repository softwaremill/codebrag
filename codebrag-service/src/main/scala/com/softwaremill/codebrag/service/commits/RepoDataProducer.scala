package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.service.config.RepositoryConfig
import com.typesafe.scalalogging.slf4j.Logging
import org.eclipse.jgit.util.StringUtils
import org.eclipse.jgit.transport._
import com.jcraft.jsch.Session
import com.typesafe.config.ConfigFactory

class RepoDataProducer(userDao: UserDAO, config: RepositoryConfig) extends Logging {

  def createFromConfiguration(): Option[RepoData] = {
    config.repositoryType match {
      case "github" => createGithubConfig()
      case "git-https" => createHttpsGitConfig()
      case "git-ssh" => createGitSshConfig()
      case _ => throw new IllegalArgumentException(s"Unknown repository type: ${config.repositoryType}")
    }
  }

  private def createHttpsGitConfig() = {
    logger.info(s"Using repo config: git-https, name: ${config.gitHttpsRepositoryName}, uri: ${config.gitHttpsRepositoryUri}")

    Some(new GitRepoData(config.gitHttpsRepositoryName, config.gitHttpsRepositoryUri,
      config.gitHttpsRepositoryUsername, config.gitHttpsRepositoryPassword))
  }

  private def createGitSshConfig() = {
    logger.info(s"Using repo config: git-ssh, name: ${config.gitSshRepositoryName}, uri: ${config.gitSshRepositoryUri}")
    class MyJschConfigSessionFactory(sshPassphraseCredentialsProvider: CredentialsProvider) extends JschConfigSessionFactory {
      def configure(hc: OpenSshConfig.Host, session: Session) {
        val userInfo = new CredentialsProviderUserInfo(session, sshPassphraseCredentialsProvider)
        session.setUserInfo(userInfo)
      }
    }
    val sshGitRepoData = new GitSshRepoData(config.gitSshRepositoryName, config.gitSshRepositoryUri, config.gitSshPassphrase)
    SshSessionFactory.setInstance(new MyJschConfigSessionFactory(sshGitRepoData.credentials))
    Some(sshGitRepoData)
  }

  private def createGithubConfig() = {
    logger.info(s"Using repo config: github, owner: ${config.githubRepositoryOwner}, name: ${config.githubRepositoryName}")

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
        ""
      }
    }

    token
  }
}
