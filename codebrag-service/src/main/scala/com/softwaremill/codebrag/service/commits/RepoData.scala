package com.softwaremill.codebrag.service.commits

import java.nio.file.Path
import org.eclipse.jgit.transport.{URIish, CredentialItem, UsernamePasswordCredentialsProvider, CredentialsProvider}

trait RepoData {
  def remoteUri: String
  def branch: String
  def localPathRelativeTo(path: Path): Path
  def credentials: CredentialsProvider
  def credentialsValid: Boolean
  def repositoryName: String
}

case class GitHubRepoData(repoOwner: String, repositoryName: String, branch: String, token: String) extends RepoData {
  def remoteUri = s"https://github.com/$repoOwner/$repositoryName.git"
  def localPathRelativeTo(path: Path) = path.resolve(repoOwner).resolve(repositoryName)
  def credentials = new UsernamePasswordCredentialsProvider(token, "")
  def credentialsValid = token.nonEmpty
}

case class GitRepoData(repositoryName: String, uri: String, branch: String, username: String, password: String) extends RepoData {
  def remoteUri = uri
  def localPathRelativeTo(path: Path) = path.resolve(repositoryName)
  def credentials = new UsernamePasswordCredentialsProvider(username, password)
  def credentialsValid = true
}

case class GitSshRepoData(repositoryName: String, uri: String, branch: String, passphrase: String) extends RepoData {
  def remoteUri = uri
  def localPathRelativeTo(path: Path) = path.resolve(repositoryName)
  def credentials = new SshPassphraseCredentialsProvider(passphrase)
  def credentialsValid = true
}

case class SvnRepoData(repositoryName: String, uri: String, username: String, password: String) extends RepoData {
  def branch = ""
  def remoteUri = uri
  def localPathRelativeTo(path: Path) = path.resolve(repositoryName)
  def credentialsValid = true
  def credentials = null
}

class SshPassphraseCredentialsProvider(passphrase: String) extends CredentialsProvider {
  def isInteractive = false
  def supports(items: CredentialItem*) = true
  def get(uri: URIish, items: CredentialItem*) = {
    if(passphrase.nonEmpty) {
      items.foreach { item => {
        item match {
          case i: CredentialItem.StringType => i.setValue(passphrase)
          case _ => {}
        }
      }}
    }
    true
  }
}


