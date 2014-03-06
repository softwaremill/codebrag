package com.softwaremill.codebrag.service.commits.branches

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.repository.Repository
import com.softwaremill.codebrag.domain.MultibranchLoadCommitsResult


/**
 * Keeps commits (SHA) for all repository branches
 */
class RepositoryCache(backend: PersistentBackendForCache) extends Logging {

  // TODO: make configurable
  private val MaxCommitsPerBranchCount = 10


  // TODO: probably change this
  private val commits = new scala.collection.mutable.HashMap[String, List[String]]

  def addCommits(loadResult: MultibranchLoadCommitsResult) {
    backend.persist(loadResult)
    loadResult.commits.foreach { branchCommits =>
      val commitsShas = branchCommits.commits.map(_.sha)
      addCommitsToBranch(commitsShas, branchCommits.branchName)
    }
  }

  private def addCommitsToBranch(newCommits: List[String], branchName: String) {
    logger.debug(s"Adding ${newCommits.size} to ${branchName}")
    val finalCommits = commits.get(branchName) match {
      case Some(commits) => newCommits ::: commits
      case None => newCommits
    }
    // TODO: cut from the bottom if exceeds max limit
    commits.put(branchName, finalCommits)
    logger.debug(s"Final number of commits in ${branchName}: ${commits.get(branchName).getOrElse(List.empty).size}")
  }

  def getCommitsForBranchExcluding(commitsToExclude: List[String], branchName: String): List[String] = {
    logger.debug(s"Getting branch commits for ${branchName} and excluding ${commitsToExclude.size} commits")
    commits.get(branchName) match {
      case Some(commits) => commits.filterNot(commitsToExclude.contains)
      case None => List.empty
    }
  }

  def initializeWith(repository: Repository, lastKnownBranchPointers: Map[String, String]) {
    logger.debug(s"Initializing repo cache")
    val savedState = backend.loadBranchesState()
    val loadResult = repository.loadLastKnownRepoState(savedState, MaxCommitsPerBranchCount)
    loadResult.commits.foreach { branchCommits =>
      val commitsShas = branchCommits.commits.map(_.sha)
      logger.debug(s"Adding ${commitsShas.size} commits to ${branchCommits.branchName}")
      addCommitsToBranch(commitsShas, branchCommits.branchName)
    }
    logger.debug(s"Cache initialized")
  }

}