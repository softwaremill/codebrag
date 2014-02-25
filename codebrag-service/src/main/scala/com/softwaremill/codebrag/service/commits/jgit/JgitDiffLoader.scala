package com.softwaremill.codebrag.service.commits.jgit

import com.softwaremill.codebrag.repository.config.RepoData
import com.softwaremill.codebrag.domain.CommitFileInfo
import com.softwaremill.codebrag.service.commits.DiffLoader

class JgitDiffLoader extends DiffLoader with JgitDiffExtractor {
  def loadDiff(sha: String, repoData: RepoData): Option[List[CommitFileInfo]] = {
    val repo = repoData.buildRepository
    for {
      commit <- repo.getCommit(sha)
    } yield {
      extractDiffsFromCommit(commit, repo.repo)
    }
  }
}
