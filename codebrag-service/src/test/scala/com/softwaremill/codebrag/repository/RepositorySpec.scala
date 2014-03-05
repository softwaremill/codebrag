package com.softwaremill.codebrag.repository

import com.softwaremill.codebrag.repository.config.RepoData
import com.softwaremill.codebrag.service.commits.jgit.TemporaryGitRepo
import com.softwaremill.codebrag.domain.MultibranchLoadCommitsResult

trait RepositorySpec {

  class TestRepository(val repoData: RepoData) extends Repository {
    protected def pullChangesForRepo() = ???
  }

  def repoData(repo: TemporaryGitRepo) = RepoData(repo.tempDir.getAbsolutePath, "temp", "git", None)

  def loadResultForBranch(branchName: String, loadResult: MultibranchLoadCommitsResult) = {
    loadResult.commits.find(_.branchName == branchName).get
  }

}