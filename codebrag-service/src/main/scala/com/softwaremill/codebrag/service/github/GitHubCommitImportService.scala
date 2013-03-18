package com.softwaremill.codebrag.service.github

import org.eclipse.egit.github.core.service.CommitService
import org.eclipse.egit.github.core.{RepositoryCommit, IRepositoryIdProvider}
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
    dao.storeCommits(commits)
  }
}


