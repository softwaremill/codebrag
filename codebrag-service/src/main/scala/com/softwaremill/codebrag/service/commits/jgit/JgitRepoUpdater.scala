package com.softwaremill.codebrag.service.commits.jgit

import java.nio.file.Path
import com.softwaremill.codebrag.service.commits.RepoData
import org.eclipse.jgit.lib.ObjectId
import com.typesafe.scalalogging.slf4j.Logging
import org.eclipse.jgit.api.LogCommand

class JgitRepoUpdater(jGitFacade: JgitFacade) extends RepoUpdater with Logging {

  def cloneFreshRepo(localPath: Path, repoData: RepoData): LogCommand = {
    val remotePath = repoData.remoteUri
    jGitFacade.clone(remotePath, repoData.branch, localPath, repoData.credentials).log()
  }

  def pullRepoChanges(localPath: Path, repoData: RepoData, previousHead : Option[ObjectId]): LogCommand = {
    val git = jGitFacade.pull(localPath, repoData.credentials)
    previousHead match {
      case Some(sha) => git.log.addRange(sha, jGitFacade.getHeadId(localPath))
      case None => {
        logger.warn("Inconsistent repository state, cannot determine last commit in database. Rebuilding from local git log.")
        git.log
      }
    }
  }

}
