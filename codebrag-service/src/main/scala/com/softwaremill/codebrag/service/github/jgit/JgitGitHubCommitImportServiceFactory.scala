package com.softwaremill.codebrag.service.github.jgit

import com.softwaremill.codebrag.service.github.{GitHubCommitImportService, GitHubCommitImportServiceFactory}
import com.softwaremill.codebrag.dao.{UserDAO, CommitInfoDAO}
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import com.softwaremill.codebrag.common.EventBus
import com.softwaremill.codebrag.service.config.CodebragConfig

class JgitGitHubCommitImportServiceFactory(commitInfoDao: CommitInfoDAO,
                                           userDao: UserDAO,
                                           eventBus: EventBus,
                                           codebragConfiguration: CodebragConfig) extends GitHubCommitImportServiceFactory {

  def createInstance(login: String): GitHubCommitImportService = {
    val importingUserToken = userDao.findByLoginOrEmail(login).get.authentication.token
    val credentials = new UsernamePasswordCredentialsProvider(importingUserToken, "")
    val uriBuilder = new GitHubRemoteUriBuilder
    new GitHubCommitImportService(
      new JgitGitHubCommitsLoader(
        new JgitFacade(credentials),
        new InternalGitDirTree(codebragConfiguration),
        new JgitLogConverter,
        uriBuilder,
        commitInfoDao),
      commitInfoDao,
      eventBus)
  }

}
