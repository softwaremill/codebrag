package com.softwaremill.codebrag.repository

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.{URIish, CredentialItem, UsernamePasswordCredentialsProvider, CredentialsProvider}
import com.softwaremill.codebrag.service.config._
import com.softwaremill.codebrag.repository.config.{UserPassCredentials, GitRepoConfig, PassphraseCredentials}

/**
 * Encapsulates all required operations on already initialized git repo
 * It works on current branch only and can pull changes and list commits
 * either all or only those to specified commit (starting from HEAD)
 * @param repoConfig repository config required to work on repo (location, credentials)
 */
class GitRepository(val repoConfig: GitRepoConfig) extends Repository {

  def repoConfig2 = repoConfig
  val name = repoConfig.repoName

  private val credentialsProvider = {
    repoConfig.credentials.map { c =>
      c match {
        case ssh: PassphraseCredentials => new SshPassphraseCredentialsProvider(ssh.phrase)
        case userpass: UserPassCredentials => new UsernamePasswordCredentialsProvider(userpass.user, userpass.pass)
      }
    }
  }

  def pullChanges {
    logger.debug(s"Pulling changes for ${repoConfig.repoLocation}")
    try {
      val pullCommand = new Git(repo).pull()
      credentialsProvider.foreach(pullCommand.setCredentialsProvider(_))
      pullCommand.call()
      logger.debug(s"Changes pulled succesfully")
    } catch {
      case e: Exception => throw new RuntimeException(s"Cannot pull changes for repo: ${repoConfig.repoLocation}", e)
    }
  }

  private class SshPassphraseCredentialsProvider(passphrase: String) extends CredentialsProvider {
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

}