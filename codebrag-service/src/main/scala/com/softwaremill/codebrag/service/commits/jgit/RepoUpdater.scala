package com.softwaremill.codebrag.service.commits.jgit

import java.nio.file.Path
import com.softwaremill.codebrag.service.commits.{RepoData}
import org.eclipse.jgit.api.LogCommand
import org.eclipse.jgit.lib.ObjectId

trait RepoUpdater {

  def pullRepoChanges(localPath: Path, repoData: RepoData, previousHead : Option[ObjectId]): LogCommand

  def cloneFreshRepo(localPath: Path, repoData: RepoData): LogCommand

}
