package com.softwaremill.codebrag.service.github.egit

import org.eclipse.egit.github.core.service.CommitService
import com.softwaremill.codebrag.dao.CommitInfoDAO
import com.softwaremill.codebrag.domain.CommitInfo
import org.eclipse.egit.github.core.IRepositoryIdProvider
import scala.collection.JavaConversions._
import com.softwaremill.codebrag.service.github.{GitHubCommitsLoader, GitHubCommitInfoConverter}

class EgitGitHubCommitsLoader(val commitService: CommitService, val commitInfoDao: CommitInfoDAO, val commitInfoConverter: GitHubCommitInfoConverter) extends GitHubCommitsLoader {

  override def loadMissingCommits(repoOwner: String, repoName: String): List[CommitInfo] = {
    val repository = GitHubRepositoryIdProvider(repoOwner, repoName)
    val githubCommits = commitService.getCommits(repository)
    val storedShas = commitInfoDao.findAllSha()
    githubCommits
      .filterNot(c => storedShas.contains(c.getSha))
      .map(commit => {
        val githubCommitDetails = commitService.getCommit(repository, commit.getSha)
        commitInfoConverter.convertToCommitInfo(githubCommitDetails)
      }).toList
  }
}

case class GitHubRepositoryIdProvider(repoOwner: String, repoName: String) extends IRepositoryIdProvider {
  def generateId(): String = s"$repoOwner/$repoName"
}

