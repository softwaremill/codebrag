package com.softwaremill.codebrag.service.github

import org.eclipse.egit.github.core.service.CommitService
import org.eclipse.egit.github.core.{RepositoryCommit, IRepositoryIdProvider}
import com.softwaremill.codebrag.dao.CommitInfoDAO
import scala.collection.JavaConversions._
import com.typesafe.scalalogging.slf4j.Logging

class GitHubCommitImportService(commitService: CommitService, converter: GitHubCommitInfoConverter, dao: CommitInfoDAO) extends Logging {
  def repoId(owner: String, repo: String) = {
    new IRepositoryIdProvider {
      def generateId(): String = s"$owner/$repo"
    }
  }

  def importRepoCommits(owner: String, repo: String) {
    val commitsReadStart = System.currentTimeMillis()
    val commits: List[RepositoryCommit] = commitService.getCommits(repoId(owner, repo)).toList
    logger.debug(s"Reading ${commits.size} commits from repository took ${System.currentTimeMillis() - commitsReadStart}ms")
    val storedShas: List[String] = dao.findAll() map (_.sha)
    val newCommits: List[RepositoryCommit] = commits filter (commit => !storedShas.contains(commit.getSha))
    val importStartTime = System.currentTimeMillis()
    newCommits foreach (commit => importCommitDetails(commit.getSha, owner, repo))
    logger.debug(s"Importing data for ${newCommits.size} new commits took ${System.currentTimeMillis() - importStartTime}ms")
  }

  private def importCommitDetails(commitId: String, owner: String, repo: String) {
    val commit = commitService.getCommit(repoId(owner, repo), commitId)
    dao.storeCommit(converter.convertToCommitInfo(commit))
  }
}

class GitHubCommitImportServiceFactory(provider: GitHubClientProvider, converter: GitHubCommitInfoConverter, commitInfoDao: CommitInfoDAO) {
  def createInstance(email: String): GitHubCommitImportService = {
    new GitHubCommitImportService(new CommitService(provider.getGitHubClient(email)), converter, commitInfoDao)
  }
}


