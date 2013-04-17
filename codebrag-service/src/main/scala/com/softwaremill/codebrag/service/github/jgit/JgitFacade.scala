package com.softwaremill.codebrag.service.github.jgit

import org.eclipse.jgit.api._
import java.nio.file.{Paths, Path}
import org.eclipse.jgit.storage.file.FileRepository
import org.eclipse.jgit.lib.{Constants, ObjectId}

class JgitFacade {

  def clone(remote: String, localPath: Path): Git = new CloneCommand().setURI(remote).setDirectory(localPath.toFile).call()

  def pull(localPath: Path): Git = {

    val repository = getRepository(localPath)
    val git = new Git(repository)

    val pullResult = git.pull().call()
    if (!pullResult.isSuccessful) throw new IllegalStateException(s"Git pull to $localPath failed. Cause: $pullResult")
    git
  }

  def getHeadId(localPath: Path): ObjectId = {
    val repository = getRepository(localPath)
    repository.resolve(Constants.HEAD)
  }

  private def getRepository(localGitPath: Path): FileRepository = {
    val internalPath = internalGitPath(localGitPath)
    new FileRepository(internalPath.toFile)
  }

  private def internalGitPath(localGitPath: Path): Path = Paths.get(s"$localGitPath/.git").normalize()
}