package com.softwaremill.codebrag.service.github.egit

import com.softwaremill.codebrag.dao.CommitInfoDAO
import org.eclipse.egit.github.core.service.CommitService
import com.softwaremill.codebrag.service.github._

class EgitGitHubCommitImportServiceFactory(provider: GitHubClientProvider, commitInfoConverter: GitHubCommitInfoConverter, commitInfoDao: CommitInfoDAO, reviewTasksGenerator: CommitReviewTaskGenerator)
  extends GitHubCommitImportServiceFactory {
  override def createInstance(login: String): GitHubCommitImportService = {
    val commitService = new CommitService(provider.getGitHubClient(login))
    val commitsLoader = new EgitGitHubCommitsLoader(commitService, commitInfoDao, commitInfoConverter)
    new GitHubCommitImportService(commitsLoader, commitInfoDao, reviewTasksGenerator)
  }
}
