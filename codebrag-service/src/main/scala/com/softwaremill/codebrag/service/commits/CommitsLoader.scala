package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.repository.config.RepoData
import com.softwaremill.codebrag.service.commits.jgit.LoadCommitsResult

trait CommitsLoader {
  def loadNewCommits(repoData: RepoData): LoadCommitsResult
}
