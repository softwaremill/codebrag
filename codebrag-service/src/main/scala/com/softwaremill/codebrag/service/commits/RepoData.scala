package com.softwaremill.codebrag.service.commits

import java.nio.file.Path
import org.eclipse.jgit.transport.{URIish, CredentialItem, UsernamePasswordCredentialsProvider, CredentialsProvider}

trait RepoData {
  def remoteUri: String
  def localPathRelativeTo(path: Path): Path
  def credentials: CredentialsProvider
  def credentialsValid: Boolean
}

case class GitHubRepoData(repoOwner: String, repoName: String, token: String) extends RepoData {
  def remoteUri = s"https://github.com/$repoOwner/$repoName.git"
  def localPathRelativeTo(path: Path) = path.resolve(repoOwner).resolve(repoName)
  def credentials = new UsernamePasswordCredentialsProvider(token, "")
  def credentialsValid = token.nonEmpty
}

case class GitRepoData(name: String, uri: String, username: String, password: String) extends RepoData {
  def remoteUri = uri
  def localPathRelativeTo(path: Path) = path.resolve(name)
  def credentials = new UsernamePasswordCredentialsProvider(username, password)
  def credentialsValid = (password != null && password.nonEmpty)
}

case class GitSshRepoData(name: String, uri: String, passphrase: String) extends RepoData {
  def remoteUri = uri
  def localPathRelativeTo(path: Path) = path.resolve(name)
  def credentials = new SshPassphraseCredentialsProvider(passphrase)
  def credentialsValid = (passphrase != null && passphrase.nonEmpty)

}

class SshPassphraseCredentialsProvider(passphrase: String) extends CredentialsProvider {
  def isInteractive = false
  def supports(items: CredentialItem*) = true
  def get(uri: URIish, items: CredentialItem*) = {
    items.foreach(_.asInstanceOf[CredentialItem.StringType].setValue(passphrase))
    true
  }
}

