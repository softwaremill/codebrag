package com.softwaremill.codebrag.service.github.jgit

import java.nio.file.{Paths, Path}
import com.softwaremill.codebrag.service.config.CodebragConfiguration

class InternalGitDirTree {

  def containsRepo(owner: String, repoName: String): Boolean = {
    getPath(owner, repoName).toFile.exists()
  }

  def getPath(owner: String, repoName: String): Path = Paths.get(s"${InternalGitDirTree.Root}/$owner/$repoName")
}

object InternalGitDirTree {

  val Root = (
    if (!CodebragConfiguration.localGitPath.equals(""))
      CodebragConfiguration.localGitPath
    else ".") + "/repos"
}