package com.softwaremill.codebrag.service.commits.jgit

import com.softwaremill.codebrag.service.commits.{RepoData, CommitsLoader}
import com.softwaremill.codebrag.domain.CommitInfo
import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.JavaConversions._
import com.softwaremill.codebrag.dao.RepositoryHeadDao
import org.eclipse.jgit.lib.ObjectId

class JgitCommitsLoader(jGitFacade: JgitFacade, internalDirTree: InternalGitDirTree, converter: JgitLogConverter,
                              repoHeadDao: RepositoryHeadDao) extends CommitsLoader with Logging {

  def loadMissingCommits(repoData: RepoData): List[CommitInfo] = {

    val remotePath = repoData.remoteUri
    val localPath = internalDirTree.getPath(repoData)

    val logCommand = if (!internalDirTree.containsRepo(repoData))
      jGitFacade.clone(remotePath, localPath, repoData.credentials).log()
    else {
      val previousHead = fetchPreviousHead(repoData)
      val git = jGitFacade.pull(localPath, repoData.credentials)
      val headAfterPull = jGitFacade.getHeadId(localPath)
      repoHeadDao.update(repoData.remoteUri, ObjectId.toString(headAfterPull))
      previousHead match {
        case Some(sha) => git.log.addRange(sha, headAfterPull)
        case None => {
          logger.warn("Incosistent repository state, cannot determine last commit in database. Rebuilding from local git log.")
          git.log
        }
      }
    }
    converter.toCommitInfos(logCommand.call().toList, logCommand.getRepository)
  }

  def fetchPreviousHead(repoData: RepoData): Option[ObjectId] = {
    repoHeadDao.get(repoData.remoteUri).map(ObjectId.fromString(_))
  }

}