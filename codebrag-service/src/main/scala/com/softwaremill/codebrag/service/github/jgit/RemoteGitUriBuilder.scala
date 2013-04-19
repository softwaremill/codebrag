package com.softwaremill.codebrag.service.github.jgit

object RemoteGitUriBuilder {
  type OwnerName = String
  type RepositoryName = String
  type RemoteUri = String
  type BuildFunction = (OwnerName, RepositoryName) => RemoteUri
}