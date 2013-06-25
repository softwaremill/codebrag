package com.softwaremill.codebrag.service.github.jgit

import com.softwaremill.codebrag.service.github.RepoData

trait RemoteGitUriBuilder {
  def build(repoData: RepoData): String
}