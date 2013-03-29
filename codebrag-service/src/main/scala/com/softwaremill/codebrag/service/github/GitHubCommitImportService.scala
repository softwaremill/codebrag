package com.softwaremill.codebrag.service.github

import org.eclipse.egit.github.core.service.CommitService
import org.eclipse.egit.github.core.{RepositoryCommit, IRepositoryIdProvider}
import com.softwaremill.codebrag.dao.CommitInfoDAO
import scala.collection.JavaConversions._

class GitHubCommitImportService(commitService: CommitService, converter: GitHubCommitInfoConverter, dao: CommitInfoDAO) {


  def repoId(owner: String, repo: String) = {
    new IRepositoryIdProvider {
      def generateId(): String = s"$owner/$repo"
    }
  }

  def importRepoCommits(owner: String, repo: String) {
    val commits: List[RepositoryCommit] = commitService.getCommits(repoId(owner, repo)).toList
    val storedShas: List[String] = dao.findAll() map (_.sha)
    val newCommits: List[RepositoryCommit] = commits filter (commit => !storedShas.contains(commit.getSha))
    newCommits foreach (commit => importCommitDetails(commit.getSha, owner, repo))
  }

  private def importCommitDetails(commitId: String, owner: String, repo: String) {
    val commit = commitService.getCommit(repoId(owner, repo), commitId)
    dao.storeCommit(converter.convertToCommitInfo(commit))
  }
}


