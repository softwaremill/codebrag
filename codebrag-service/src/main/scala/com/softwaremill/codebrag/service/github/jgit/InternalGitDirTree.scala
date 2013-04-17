package com.softwaremill.codebrag.service.github.jgit

import java.nio.file.{Paths, Path}

class InternalGitDirTree {

  def containsRepo(owner: String, repoName: String): Boolean = {
    getPath(owner, repoName).toFile.exists()
  }

  def getPath(owner: String, repoName: String): Path = Paths.get(s"${InternalGitDirTree.Root}/$owner/$repoName")
}

object InternalGitDirTree {
  val Root = "./repos"
}