package com.softwaremill.codebrag.service.commits.jgit

import com.softwaremill.codebrag.service.commits.CommitsLoader
import com.softwaremill.codebrag.domain.{LoadCommitsResult, RepositoryStatus, CommitInfo}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO
import org.eclipse.jgit.lib.ObjectId
import com.softwaremill.codebrag.repository.Repository
import com.softwaremill.codebrag.repository.config.RepoData


class JgitCommitsLoader(converter: JgitLogConverter, repoStatusDao: RepositoryStatusDAO) extends CommitsLoader with Logging {

  def loadNewCommits(repoData: RepoData) = {
    val repo = Repository.buildUsing(repoData)
    val newCommits = updateAndGetCommits(repo)
    val commitInfos = converter.toCommitInfos(newCommits, repo.repo)
    LoadCommitsResult(commitInfos, repo.repoName, ObjectId.toString(repo.currentHead))
  }

  private def updateAndGetCommits(repo: Repository) = {
    val lastKnownCommitSHA = repoStatusDao.get(repo.repoName)
    try {
      repo.pullChanges()
      repo.getCommits(lastKnownCommitSHA)
    } catch {
      case e: Exception => {
        logger.error(s"Could not pull repo or load new commits: ${e.getMessage}")
        throw e
      }
    }
  }

}