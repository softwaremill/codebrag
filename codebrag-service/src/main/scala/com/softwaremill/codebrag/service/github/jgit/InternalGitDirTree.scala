package com.softwaremill.codebrag.service.github.jgit

import java.nio.file.{Paths, Path}
import com.softwaremill.codebrag.service.config.CodebragConfig
import org.eclipse.jgit.util.StringUtils._
import com.softwaremill.codebrag.service.github.RepoData

class InternalGitDirTree(codebragConfig: CodebragConfig) {
  val root = (
    if (!isEmptyOrNull(codebragConfig.localGitStoragePath))
      codebragConfig.localGitStoragePath
    else ".") + "/repos"

  def containsRepo(repoData: RepoData): Boolean = {
    getPath(repoData).toFile.exists()
  }

  def getPath(repoData: RepoData): Path = Paths.get(s"$root/${repoData.repoOwner}/${repoData.repoName}")
}
