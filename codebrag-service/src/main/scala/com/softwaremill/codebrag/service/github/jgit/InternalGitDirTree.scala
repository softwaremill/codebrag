package com.softwaremill.codebrag.service.github.jgit

import java.nio.file.{Paths, Path}
import com.softwaremill.codebrag.service.config.CodebragConfig
import org.eclipse.jgit.util.StringUtils._

class InternalGitDirTree(codebragConfig: CodebragConfig) {
  val root = (
    if (!isEmptyOrNull(codebragConfig.localGitStoragePath))
      codebragConfig.localGitStoragePath
    else ".") + "/repos"

  def containsRepo(owner: String, repoName: String): Boolean = {
    getPath(owner, repoName).toFile.exists()
  }

  def getPath(owner: String, repoName: String): Path = Paths.get(s"$root/$owner/$repoName")
}
