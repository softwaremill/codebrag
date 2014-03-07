package com.softwaremill.codebrag.service.commits.jgit

import com.softwaremill.codebrag.domain.CommitFileInfo
import com.softwaremill.codebrag.service.commits.DiffLoader
import com.softwaremill.codebrag.repository.Repository

class JgitDiffLoader extends DiffLoader with JgitDiffExtractor {
  def loadDiff(sha: String, repo: Repository): Option[List[CommitFileInfo]] = {
    for {
      commit <- repo.getCommit(sha)
    } yield {
      extractDiffsFromCommit(commit, repo.repo)
    }
  }
}
