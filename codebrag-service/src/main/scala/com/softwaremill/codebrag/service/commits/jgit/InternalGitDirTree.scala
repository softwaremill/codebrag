package com.softwaremill.codebrag.service.commits.jgit

import java.nio.file.{Paths, Path}
import com.softwaremill.codebrag.service.config.CodebragConfig
import org.eclipse.jgit.util.StringUtils._
import com.softwaremill.codebrag.service.commits.RepoData

class InternalGitDirTree(codebragConfig: CodebragConfig) {
  val root = {
    val baseDir = if (!isEmptyOrNull(codebragConfig.localGitStoragePath))
      codebragConfig.localGitStoragePath
    else
      "."

    Paths.get(baseDir).resolve("repos")
  }

  def containsRepo(repoData: RepoData): Boolean = getPath(repoData).toFile.exists()

  def getPath(repoData: RepoData): Path = repoData.localPathRelativeTo(root)
}
