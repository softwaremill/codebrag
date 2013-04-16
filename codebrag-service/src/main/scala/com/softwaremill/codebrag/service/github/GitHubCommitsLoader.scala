package com.softwaremill.codebrag.service.github

import com.softwaremill.codebrag.domain.CommitInfo

trait GitHubCommitsLoader {
  def loadMissingCommits(repoOwner: String, repoName: String): List[CommitInfo]
}
