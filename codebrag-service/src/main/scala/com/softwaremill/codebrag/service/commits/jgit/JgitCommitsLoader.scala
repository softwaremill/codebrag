package com.softwaremill.codebrag.service.commits.jgit

import com.softwaremill.codebrag.service.commits.{RepoData, CommitsLoader}
import com.softwaremill.codebrag.domain.{RepositoryStatus, CommitInfo}
import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.JavaConversions._
import com.softwaremill.codebrag.dao.RepositoryStatusDAO
import org.eclipse.jgit.lib.{Constants, ObjectId}
import org.eclipse.jgit.api.LogCommand

class JgitCommitsLoader(jGitFacade: JgitFacade, internalDirTree: InternalDirTree, converter: JgitLogConverter,
                              repoStatusDao: RepositoryStatusDAO, repoUpdater : RepoUpdater)
  extends CommitsLoader with Logging {

  def loadMissingCommits(repoData: RepoData): List[CommitInfo] = {
    if(repoData.credentialsValid) {
      loadCommits(repoData)
    } else {
      dontLoadCommits
    }
  }

  private def loadCommits(repoData: RepoData): List[CommitInfo] = {
    val logCommand = try {
      val updateResult = updateRepository(repoData)
      val currentHead = ObjectId.toString(updateResult.getRepository.resolve(Constants.HEAD)) 
      val repoReadyStatus = RepositoryStatus.ready(repoData.repositoryName).withHeadId(currentHead)
      repoStatusDao.updateRepoStatus(repoReadyStatus)
      updateResult
    } catch {
      case e: Exception => {
        val repoNotReadyStatus = RepositoryStatus.notReady(repoData.repositoryName, Some(e.getMessage))
        repoStatusDao.updateRepoStatus(repoNotReadyStatus)
        throw e
      }
    }
    converter.toCommitInfos(logCommand.call().toList, logCommand.getRepository)
  }


  def updateRepository(repoData: RepoData) = {
    val localPath = internalDirTree.getPath(repoData)
    val logCommand = if (!internalDirTree.containsRepo(repoData)) {
      repoUpdater.cloneFreshRepo(localPath, repoData)
    } else {
      repoUpdater.pullRepoChanges(localPath, repoData, fetchPreviousHead(repoData))
    }
    logCommand
  }

  private def dontLoadCommits = {
    logger.warn("Invalid repository data, can't import commits")
    List.empty
  }

  private def fetchPreviousHead(repoData: RepoData): Option[ObjectId] = {
    repoStatusDao.get(repoData.repositoryName).map(ObjectId.fromString(_))
  }

}