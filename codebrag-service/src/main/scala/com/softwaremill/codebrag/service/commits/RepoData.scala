package com.softwaremill.codebrag.service.commits

import java.nio.file.Path
import org.eclipse.jgit.transport.{UsernamePasswordCredentialsProvider, CredentialsProvider}

trait RepoData {
  def remoteUri: String
  def localPathRelativeTo(path: Path): Path
  def credentials: CredentialsProvider
}

case class GitHubRepoData(repoOwner: String, repoName: String, token: String) extends RepoData {
  def remoteUri = s"https://github.com/$repoOwner/$repoName.git"
  def localPathRelativeTo(path: Path) = path.resolve(repoOwner).resolve(repoName)
  def credentials = new UsernamePasswordCredentialsProvider(token, "")
}

case class GitRepoData(name: String, uri: String, username: String, password: String) extends RepoData {
  def remoteUri = uri
  def localPathRelativeTo(path: Path) = path.resolve(name)
  def credentials = new UsernamePasswordCredentialsProvider(username, password)
}
