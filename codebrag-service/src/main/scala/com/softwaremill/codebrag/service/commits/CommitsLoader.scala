package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.domain.CommitInfo
import com.softwaremill.codebrag.repository.config.RepoConfig

trait CommitsLoader {
  def loadNewCommits(repoData: RepoConfig): List[CommitInfo]
}
