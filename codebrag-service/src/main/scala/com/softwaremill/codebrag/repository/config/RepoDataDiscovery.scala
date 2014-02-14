package com.softwaremill.codebrag.repository.config

import com.softwaremill.codebrag.service.config.RepositoryConfig
import java.nio.file.{Path, Paths}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File
import com.typesafe.config.ConfigFactory

object RepoDataDiscovery {

  def discoverRepoDataFromConfig(repositoryConfig: RepositoryConfig) = {
    val rootDir = resolveRootDir(repositoryConfig)
    val repoName = discoverRepoDirName(rootDir)
    val repoLocation = discoverRepoAbsolutePath(rootDir, repoName)
    val repoType = discoverRepoType(repoLocation)
    val credentials = resolveCredentials(repositoryConfig)
    RepoData(repoLocation, repoName, repoType, credentials)
  }

  private def resolveCredentials(repositoryConfig: RepositoryConfig): Option[RepoCredentials] = {
    val passphraseCredentials = repositoryConfig.passphrase.map(PassphraseCredentials(_))
    val userPassCredentials = repositoryConfig.username.map { username =>
      val password = repositoryConfig.password.getOrElse("")
      UserPassCredentials(username, password)
    }
    passphraseCredentials.orElse(userPassCredentials)
  }

  private def resolveRootDir(repositoryConfig: RepositoryConfig) = {
    val rootDir = Paths.get(repositoryConfig.repositoriesRoot)
    if(!rootDir.toFile.isDirectory) {
      throw new RuntimeException(s"Cannot find base directory for repositories (repos-root): ${repositoryConfig.repositoriesRoot}. Please ")
    }
    rootDir
  }

  private def discoverRepoDirName(rootDir: Path) = {
    val potentialRepoDirs = rootDir.toFile.listFiles.filter(_.isDirectory).map(_.getName)
    val reposRootDir = rootDir.toFile.getAbsolutePath
    if(potentialRepoDirs.isEmpty) {
      throw new RuntimeException(s"Repository directory not found in ${reposRootDir}. Please clone your repository.")
    }
    if(potentialRepoDirs.size > 1) {
      throw new RuntimeException(s"More than one directory found in ${reposRootDir}. Only one repository can exist.")
    }
    potentialRepoDirs.head
  }

  private def discoverRepoAbsolutePath(rootDir: Path, repoName: String) = {
    rootDir.resolve(repoName).toAbsolutePath.toString
  }

  private def discoverRepoType(repoLocation: String) = {
    val SvnRepoSectionName = "svn-remote"
    val repo = new FileRepositoryBuilder().setGitDir(new File(repoLocation + File.separator + ".git")).setMustExist(true).build()
    import scala.collection.JavaConversions._
    val sections = repo.getConfig.getSections.toList
    if(sections.contains(SvnRepoSectionName)) {
      "git-svn"
    } else {
      "git"
    }
  }

}