package com.softwaremill.codebrag.service.commits

import java.nio.file.Path

trait RepoData {
  def remoteUri: String
  def localPathRelativeTo(path: Path): Path
}

case class GitHubRepoData(repoOwner: String, repoName: String) extends RepoData {
  def remoteUri = s"https://github.com/$repoOwner/$repoName.git"
  def localPathRelativeTo(path: Path) = path.resolve(repoOwner).resolve(repoName)
}
