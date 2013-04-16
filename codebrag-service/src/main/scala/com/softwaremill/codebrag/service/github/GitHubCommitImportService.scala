package com.softwaremill.codebrag.service.github

import com.softwaremill.codebrag.dao.CommitInfoDAO
import com.typesafe.scalalogging.slf4j.Logging
import org.eclipse.egit.github.core.service.CommitService


class GitHubCommitImportService(commitsLoader: GithubCommitsLoader, commitInfoDao: CommitInfoDAO, reviewTasksGenerator: CommitReviewTaskGenerator) extends Logging {

  def importRepoCommits(owner: String, repo: String) {
    logger.debug("Start loading commits")
    val commitsLoaded = commitsLoader.loadMissingCommits(owner, repo)
    logger.debug(s"Commits loaded: ${commitsLoaded.size}")
    commitsLoaded.foreach(commit => {
      commitInfoDao.storeCommit(commit)
      reviewTasksGenerator.createReviewTasksFor(commit)
    })
    logger.debug("Commits stored. Loading finished.")
  }

}

class GitHubCommitImportServiceFactory(provider: GitHubClientProvider, commitInfoConverter: GitHubCommitInfoConverter, commitInfoDao: CommitInfoDAO, reviewTaskGenerator: CommitReviewTaskGenerator) {
  def createInstance(email: String): GitHubCommitImportService = {
    val commitService = new CommitService(provider.getGitHubClient(email))
    val commitsLoader = new GithubCommitsLoader(commitService, commitInfoDao, commitInfoConverter)
    new GitHubCommitImportService(commitsLoader, commitInfoDao, reviewTaskGenerator)
  }
}