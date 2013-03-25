package com.softwaremill.codebrag.service.github

import org.eclipse.egit.github.core.service.CommitService
import org.eclipse.egit.github.core.IRepositoryIdProvider
import scala.collection.JavaConversions._
import com.softwaremill.codebrag.dao.CommitInfoDAO

class GitHubCommitImportService(commitService: CommitService, converter: GitHubCommitInfoConverter, dao: CommitInfoDAO) {


  def repoId(owner: String, repo: String) = {
    new IRepositoryIdProvider {
      def generateId(): String = s"$owner/$repo"
    }
  }

  def importRepoCommits(owner: String, repo: String) {
    val commits = commitService.getCommits(repoId(owner, repo)).map(converter.convertToCommitInfo(_))
    val storedCommits = dao.findAllPendingCommits()
    dao.storeCommits(commits -- storedCommits)
  }

  def importCommitDetails(commitId: String, owner: String, repo: String) {
    val commit = commitService.getCommit(repoId(owner, repo), commitId)
    dao.storeCommit(converter.convertToCommitInfo(commit))
  }
}


