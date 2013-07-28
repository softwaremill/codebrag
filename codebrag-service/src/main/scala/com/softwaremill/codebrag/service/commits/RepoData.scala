package com.softwaremill.codebrag.service.commits

import java.nio.file.Path
import org.eclipse.jgit.transport.{URIish, CredentialItem, UsernamePasswordCredentialsProvider, CredentialsProvider}
import org.tmatesoft.svn.core.auth.{BasicAuthenticationManager, ISVNAuthenticationManager}

trait RepoData {
  def remoteUri: String
  def localPathRelativeTo(path: Path): Path
  def credentials: Object
  def credentialsValid: Boolean
}

trait BaseGitRepoData extends RepoData {
  override def credentials: CredentialsProvider
}

case class GitHubRepoData(repoOwner: String, repoName: String, token: String) extends BaseGitRepoData {
  def remoteUri = s"https://github.com/$repoOwner/$repoName.git"
  def localPathRelativeTo(path: Path) = path.resolve(repoOwner).resolve(repoName)
  def credentials = new UsernamePasswordCredentialsProvider(token, "")
  def credentialsValid = token.nonEmpty
}

case class GitRepoData(name: String, uri: String, username: String, password: String) extends BaseGitRepoData {
  def remoteUri = uri
  def localPathRelativeTo(path: Path) = path.resolve(name)
  def credentials = new UsernamePasswordCredentialsProvider(username, password)
  def credentialsValid = true
}

case class GitSshRepoData(name: String, uri: String, passphrase: String) extends BaseGitRepoData {
  def remoteUri = uri
  def localPathRelativeTo(path: Path) = path.resolve(name)
  def credentials = new SshPassphraseCredentialsProvider(passphrase)
  def credentialsValid = true

}

case class SvnRepoData(name: String, uri: String, username: String, password: String) extends RepoData {
  def remoteUri = uri
  def localPathRelativeTo(path: Path) = path.resolve(name)
  override def credentials = new BasicAuthenticationManager(username, password)
  def credentialsValid = true
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


