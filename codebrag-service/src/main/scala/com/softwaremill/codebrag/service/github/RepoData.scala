package com.softwaremill.codebrag.service.github

import java.nio.file.Path

case class RepoData(repoOwner: String, repoName: String) {
  def remoteUri = s"https://github.com/$repoOwner/$repoName.git"
  def localPathRelativeTo(path: Path) = path.resolve(repoOwner).resolve(repoName)
}
