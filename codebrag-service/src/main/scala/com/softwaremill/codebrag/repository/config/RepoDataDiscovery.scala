package com.softwaremill.codebrag.repository.config

import com.softwaremill.codebrag.service.config.RepositoryConfig
import java.nio.file.{Path, Paths}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File

object RepoDataDiscovery {

  def discoverRepoDataFromConfig(repositoryConfig: RepositoryConfig) = {
    val rootDir = resolveRootDir(repositoryConfig)
    val repoName = discoverRepoName(rootDir)
    val repoLocation = discoverRepoLocation(rootDir, repoName)
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
      val path = rootDir.toFile.getCanonicalPath
      throw new RuntimeException(s"Cannot find base directory for repositories: ${path}")
    }
    rootDir
  }

  private def discoverRepoName(rootDir: Path) = {
    val potentialRepoDirs = rootDir.toFile.listFiles.filter(_.isDirectory).map(_.getName).toList
    val reposRootDir = rootDir.toFile.getCanonicalPath
    potentialRepoDirs match {
      case Nil => throw new RuntimeException(s"Repository directory not found in ${reposRootDir}. Please clone your repository as a directory inside ${reposRootDir}")
      case dir :: Nil => dir
      case _ => throw new RuntimeException(s"More than one directory found in ${reposRootDir}. Only one repository directory can exist there")
    }
  }

  private def discoverRepoLocation(rootDir: Path, repoName: String) = {
    rootDir.resolve(repoName).toFile.getCanonicalPath
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