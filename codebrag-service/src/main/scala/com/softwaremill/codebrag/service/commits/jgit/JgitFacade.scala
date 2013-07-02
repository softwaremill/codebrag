package com.softwaremill.codebrag.service.commits.jgit

import org.eclipse.jgit.api._
import java.nio.file.{Paths, Path}
import org.eclipse.jgit.storage.file.FileRepository
import org.eclipse.jgit.lib.{Constants, ObjectId}
import org.eclipse.jgit.transport.CredentialsProvider

class JgitFacade {

  def clone(remote: String, localPath: Path, credentials: CredentialsProvider): Git =
    new CloneCommand().setURI(remote).setCredentialsProvider(credentials).setDirectory(localPath.toFile).call()

  def pull(localPath: Path, credentials: CredentialsProvider): Git = {

    val repository = getRepository(localPath)
    val git = new Git(repository)

    val pullResult = git.pull().setCredentialsProvider(credentials).call()
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