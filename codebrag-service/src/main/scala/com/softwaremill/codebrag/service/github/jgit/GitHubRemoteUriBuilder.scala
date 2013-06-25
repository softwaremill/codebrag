package com.softwaremill.codebrag.service.github.jgit

import com.softwaremill.codebrag.service.github.RepoData

class GitHubRemoteUriBuilder extends RemoteGitUriBuilder {
  def build(repoData: RepoData): String = s"https://github.com/${repoData.repoOwner}/${repoData.repoName}.git"
}
