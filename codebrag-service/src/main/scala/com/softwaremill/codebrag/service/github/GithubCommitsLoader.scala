package com.softwaremill.codebrag.service.github

import org.eclipse.egit.github.core.service.CommitService
import com.softwaremill.codebrag.dao.CommitInfoDAO
import com.softwaremill.codebrag.domain.CommitInfo
import org.eclipse.egit.github.core.IRepositoryIdProvider
import scala.collection.JavaConversions._



class GithubCommitsLoader(val commitService: CommitService, val commitInfoDao: CommitInfoDAO, val commitInfoConverter: GitHubCommitInfoConverter) {

  def loadMissingCommits(repoOwner: String, repoName: String): List[CommitInfo] = {
    val repository = GithubRepositoryIdProvider(repoOwner, repoName)
    val githubCommits = commitService.getCommits(repository)
    val storedShas = commitInfoDao.findAll().map(_.sha)
    githubCommits
      .filterNot(c => storedShas.contains(c.getSha))
      .map(commit => {
        val githubCommitDetails = commitService.getCommit(repository, commit.getSha)
        commitInfoConverter.convertToCommitInfo(githubCommitDetails)
      })
      .toList
  }

}

case class GithubRepositoryIdProvider(repoOwner: String, repoName: String) extends IRepositoryIdProvider {
  def generateId(): String = s"${repoOwner}/${repoName}"
}

