package com.softwaremill.codebrag.service.commits.jgit

import com.softwaremill.codebrag.service.commits.CommitsLoader
import com.softwaremill.codebrag.domain.{RepositoryStatus, CommitInfo}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO
import org.eclipse.jgit.lib.ObjectId
import com.softwaremill.codebrag.repository.Repository
import com.softwaremill.codebrag.repository.config.RepoData


class JgitCommitsLoader(converter: JgitLogConverter, repoStatusDao: RepositoryStatusDAO) extends CommitsLoader with Logging {

  def loadNewCommits(repoConfig: RepoData): List[CommitInfo] = {
    val repo = repoConfig.buildRepository
    val newCommits = updateAndGetCommits(repo)
    converter.toCommitInfos(newCommits, repo.repo)
  }

  private def updateAndGetCommits(repo: Repository) = {
    val lastKnownCommitSHA = repoStatusDao.get(repo.repoName)
    try {
      repo.pullChanges
      val newCommits = repo.getCommits(lastKnownCommitSHA)
      updateRepoReadyStatus(repo)
      newCommits
    } catch {
      case e: Exception => {
        updateRepoNotReadyStatus(repo, e)
        logger.debug("Could not pull repo or load new commits", e)
        throw e
      }
    }
  }

  private def updateRepoNotReadyStatus(repo: Repository, cause: Exception) {
    val repoNotReadyStatus = RepositoryStatus.notReady(repo.repoName, Some(cause.getMessage))
    repoStatusDao.updateRepoStatus(repoNotReadyStatus)
  }

  private def updateRepoReadyStatus(repo: Repository) {
    val currentHead = ObjectId.toString(repo.currentHead)
    val repoReadyStatus = RepositoryStatus.ready(repo.repoName).withHeadId(currentHead)
    repoStatusDao.updateRepoStatus(repoReadyStatus)
  }

}