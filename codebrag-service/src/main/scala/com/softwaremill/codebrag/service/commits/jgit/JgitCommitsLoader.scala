package com.softwaremill.codebrag.service.commits.jgit

import com.softwaremill.codebrag.service.commits.{RepoData, CommitsLoader}
import com.softwaremill.codebrag.domain.CommitInfo
import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.JavaConversions._
import com.softwaremill.codebrag.dao.RepositoryHeadStore
import org.eclipse.jgit.lib.ObjectId

class JgitCommitsLoader(jGitFacade: JgitFacade, internalDirTree: InternalDirTree, converter: JgitLogConverter,
                              repoHeadDao: RepositoryHeadStore, repoUpdater : RepoUpdater)
  extends CommitsLoader with Logging {

  def loadMissingCommits(repoData: RepoData): List[CommitInfo] = {
    if(repoData.credentialsValid) {
      loadCommits(repoData)
    } else {
      dontLoadCommits
    }
  }


  private def loadCommits(repoData: RepoData): List[CommitInfo] = {
    val localPath = internalDirTree.getPath(repoData)
    val logCommand = if (!internalDirTree.containsRepo(repoData)) {
      repoUpdater.cloneFreshRepo(localPath, repoData)
    } else {
      repoUpdater.pullRepoChanges(localPath, repoData, fetchPreviousHead(repoData))
    }
    converter.toCommitInfos(logCommand.call().toList, logCommand.getRepository)
  }

  private def dontLoadCommits = {
    logger.warn("Invalid repository data, can't import commits")
    List.empty
  }

  private def fetchPreviousHead(repoData: RepoData): Option[ObjectId] = {
    repoHeadDao.get(repoData.repositoryName).map(ObjectId.fromString(_))
  }

}