package com.softwaremill.codebrag.service.github.jgit

trait RemoteGitUriBuilder {
  def build(ownerName: String, repoName: String): String
}