package com.softwaremill.codebrag.service.github.jgit

import com.softwaremill.codebrag.service.github.{CommitReviewTaskGenerator, GitHubCommitImportService, GitHubCommitImportServiceFactory}
import com.softwaremill.codebrag.dao.{UserDAO, CommitInfoDAO}
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

class JgitGitHubCommitImportServiceFactory(commitInfoDao: CommitInfoDAO, reviewTaskGenerator: CommitReviewTaskGenerator, userDao: UserDAO) extends GitHubCommitImportServiceFactory {

  def createInstance(login: String): GitHubCommitImportService = {
    val importingUserToken = userDao.findByLoginOrEmail(login).get.authentication.token
    val credentials = new UsernamePasswordCredentialsProvider(importingUserToken, "")
    val uriBuilder = new GitHubRemoteUriBuilder
    new GitHubCommitImportService(new JgitGitHubCommitsLoader(new JgitFacade(credentials), new InternalGitDirTree, new JgitLogConverter, uriBuilder, commitInfoDao), commitInfoDao, reviewTaskGenerator)
  }

}
