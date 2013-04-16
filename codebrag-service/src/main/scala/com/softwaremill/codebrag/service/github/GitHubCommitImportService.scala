package com.softwaremill.codebrag.service.github

import com.softwaremill.codebrag.dao.CommitInfoDAO
import com.typesafe.scalalogging.slf4j.Logging
import org.eclipse.egit.github.core.service.CommitService


class GitHubCommitImportService(commitsLoader: GithubCommitsLoader, dao: CommitInfoDAO) extends Logging {

  def importRepoCommits(owner: String, repo: String) {
    commitsLoader.loadMissingCommits(owner, repo).foreach( dao.storeCommit(_))
  }

}

class GitHubCommitImportServiceFactory(provider: GitHubClientProvider, commitInfoConverter: GitHubCommitInfoConverter, commitInfoDao: CommitInfoDAO) {
  def createInstance(email: String): GitHubCommitImportService = {
    val commitService = new CommitService(provider.getGitHubClient(email))
    val commitsLoader = new GithubCommitsLoader(commitService, commitInfoDao, commitInfoConverter)
    new GitHubCommitImportService(commitsLoader, commitInfoDao)
  }
}


