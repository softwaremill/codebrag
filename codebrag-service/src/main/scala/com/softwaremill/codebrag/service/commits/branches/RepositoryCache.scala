package com.softwaremill.codebrag.service.commits.branches

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.repository.Repository
import com.softwaremill.codebrag.domain.{PartialCommitInfo, MultibranchLoadCommitsResult}
import com.softwaremill.codebrag.service.config.CommitCacheConfig

/**
 * Keeps commits (SHA) for all repository branches
 */
class RepositoryCache(backend: PersistentBackendForCache, config: CommitCacheConfig) extends Logging {

  // TODO: consider changing to Map[String, AtomicReference[List[CommitCacheEntry]]]
  private val commits = new scala.collection.mutable.HashMap[String, List[CommitCacheEntry]]

  def addCommits(loadResult: MultibranchLoadCommitsResult) {
    backend.persist(loadResult)
    loadResult.commits.foreach { branchCommits =>
      val cacheEntries = branchCommits.commits.map(partialCommitToCacheEntry)
      addCommitsToBranch(cacheEntries, branchCommits.branchName)
    }
  }

  private def addCommitsToBranch(newCommits: List[CommitCacheEntry], branchName: String) {
    val finalCommits = commits.get(branchName) match {
      case Some(commits) => newCommits ::: commits
      case None => newCommits
    }
    commits.put(branchName, finalCommits.take(maxCommitsPerBranchCount))
    logger.debug(s"Number of commits in ${branchName}: ${commits.get(branchName).getOrElse(List.empty).size}")
  }

  def getBranchNames = commits.keySet

  def getAllCommits = commits.flatten(_._2).toSet

  def getBranchCommits(branchName: String): List[CommitCacheEntry] = {
    commits.get(branchName).getOrElse(List.empty[CommitCacheEntry])
  }

  def initializeWith(repository: Repository) {
    logger.debug(s"Initializing repo cache")
    val savedState = backend.loadBranchesState()
    val loadResult = repository.loadLastKnownRepoState(savedState, maxCommitsPerBranchCount)
    loadResult.commits.foreach { branchCommits =>
      val cacheEntries = branchCommits.commits.map(partialCommitToCacheEntry)
      logger.debug(s"Adding ${cacheEntries.size} commits to ${branchCommits.branchName}")
      addCommitsToBranch(cacheEntries, branchCommits.branchName)
    }
    logger.debug(s"Cache initialized")
  }

  private def partialCommitToCacheEntry(commit: PartialCommitInfo): CommitCacheEntry = {
    CommitCacheEntry(commit.sha, commit.authorName, commit.authorEmail, commit.date)
  } 
  
  private def maxCommitsPerBranchCount = config.maxCommitsCachedPerBranch

}