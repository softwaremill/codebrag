package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.domain.CommitFileInfo
import com.softwaremill.codebrag.repository.Repository

trait DiffLoader {
  def loadDiff(sha: String, repo: Repository): Option[List[CommitFileInfo]]
}
