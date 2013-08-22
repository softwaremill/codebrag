package com.softwaremill.codebrag.service.commits.jgit

import org.eclipse.jgit.api._
import java.nio.file.{Paths, Path}
import org.eclipse.jgit.storage.file.FileRepository
import org.eclipse.jgit.lib.{Constants, ObjectId}
import org.eclipse.jgit.transport.CredentialsProvider
import com.typesafe.scalalogging.slf4j.Logging
import org.eclipse.jgit.util.FileUtils

class JgitFacade extends Logging {

  def clone(remote: String, branch: String, localPath: Path, credentials: CredentialsProvider): Git = {
    try {
      new CloneCommand().setURI(remote).setCredentialsProvider(credentials).setDirectory(localPath.toFile).setBranch(branch).call()
    } catch {
      case e: Exception => {
        logger.error("Could not clone. Cleanning up - removing partially initialized repository.")
        FileUtils.delete(localPath.toFile, FileUtils.RECURSIVE)
        throw e
      }
    }
  }

  def pull(localPath: Path, credentials: CredentialsProvider): Git = {

    val repository = getRepository(localPath)
    val git = new Git(repository)

    val pullResult = git.pull().setCredentialsProvider(credentials).call()
    if (!pullResult.isSuccessful) throw new IllegalStateException(s"Git pull to $localPath failed. Cause: $pullResult")
    git
  }

  def gitRepo(localPath : Path) : Git = {
    val repository = getRepository(localPath)
    new Git(repository)
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