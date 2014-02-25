package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.repository.config.RepoData
import com.softwaremill.codebrag.domain.CommitFileInfo

trait DiffLoader {
  def loadDiff(sha: String, repoData: RepoData): Option[List[CommitFileInfo]]
}
