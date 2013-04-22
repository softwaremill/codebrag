package com.softwaremill.codebrag.service.github.jgit

class GitHubRemoteUriBuilder extends RemoteGitUriBuilder {
  def build(ownerName: String, repoName: String): String = s"https://github.com/$ownerName/$repoName.git"
}
