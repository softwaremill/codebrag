package com.softwaremill.codebrag.service.github

import com.softwaremill.codebrag.dao.CommitInfoDAO
import com.typesafe.scalalogging.slf4j.Logging

class GitHubCommitImportService(commitsLoader: GitHubCommitsLoader, commitInfoDao: CommitInfoDAO, reviewTasksGenerator: CommitReviewTaskGenerator) extends Logging {

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