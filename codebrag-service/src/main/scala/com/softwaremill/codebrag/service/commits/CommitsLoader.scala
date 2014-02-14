package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.domain.CommitInfo
import com.softwaremill.codebrag.repository.config.RepoData

trait CommitsLoader {
  def loadNewCommits(repoData: RepoData): List[CommitInfo]
}
