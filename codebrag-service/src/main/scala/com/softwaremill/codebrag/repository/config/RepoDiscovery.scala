package com.softwaremill.codebrag.repository.config

import com.softwaremill.codebrag.service.config.RepositoryConfig
import java.nio.file.{Path, Paths}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File

trait RepoDiscovery {

  def repositoryConfig: RepositoryConfig

  private val rootDir = Paths.get(repositoryConfig.repositoriesRoot).resolve("repos")

  val repoName = discoverRepoDirName(rootDir)
  val repoLocation = discoverRepoAbsolutePath(rootDir, repoName)
  val repoType = discoverRepoType(repoLocation)


  private def discoverRepoDirName(rootDir: Path) = {
    val potentialRepoDirs = rootDir.toFile.listFiles.filter(_.isDirectory).map(_.getName)
    if(potentialRepoDirs.isEmpty) {
      throw new RuntimeException("Repository directory not found. Please clone your repository.")
    }
    if(potentialRepoDirs.size > 1) {
      throw new RuntimeException("More than one directory found. Only one repository can exist.")
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