package com.softwaremill.codebrag.service.github

import com.softwaremill.codebrag.dao.UserDAO
import org.eclipse.egit.github.core.client.GitHubClient
import com.typesafe.scalalogging.slf4j.Logging

class GitHubClientProvider(userDao: UserDAO) extends Logging {
  def getGitHubClient(login: String) = {
    val client = new GitHubClient()
    userDao.findByLoginOrEmail(login) match {
      case Some(user) => {
        logger.info(s"Token type: ${user.authentication.provider}, value: ${user.authentication.token}")
        client.setOAuth2Token(user.authentication.token)
      }
      case None => {
        logger.info("Token not found")
        client
      }
    }
  }
}
