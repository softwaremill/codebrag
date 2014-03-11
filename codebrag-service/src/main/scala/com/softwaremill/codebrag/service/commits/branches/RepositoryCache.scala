package com.softwaremill.codebrag.service.commits.branches

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.repository.Repository
import com.softwaremill.codebrag.domain.{CommitLike, PartialCommitInfo, MultibranchLoadCommitsResult}

case class CommitCacheEntry(sha: String, authorName: String, authorEmail: String)

object CommitCacheEntry {

  implicit object CommitLikeCommitCacheEntry extends CommitLike[CommitCacheEntry] {
    def authorName(commitLike: CommitCacheEntry) = commitLike.authorName
    def authorEmail(commitLike: CommitCacheEntry) = commitLike.authorEmail
  }

}

/**
 * Keeps commits (SHA) for all repository branches
 */
class RepositoryCache(backend: PersistentBackendForCache) extends Logging {

  // TODO: make configurable
  private val MaxCommitsPerBranchCount = 10


  // TODO: probably change this
  private val commits = new scala.collection.mutable.HashMap[String, List[CommitCacheEntry]]

  def addCommits(loadResult: MultibranchLoadCommitsResult) {
    backend.persist(loadResult)
    loadResult.commits.foreach { branchCommits =>
      val cacheEntries = branchCommits.commits.map(partialCommitToCacheEntry)
      addCommitsToBranch(cacheEntries, branchCommits.branchName)
    }
  }

  private def addCommitsToBranch(newCommits: List[CommitCacheEntry], branchName: String) {
    logger.debug(s"Adding ${newCommits.size} to ${branchName}")
    val finalCommits = commits.get(branchName) match {
      case Some(commits) => newCommits ::: commits
      case None => newCommits
    }
    // TODO: cut from the bottom if exceeds max limit
    commits.put(branchName, finalCommits)
    logger.debug(s"Final number of commits in ${branchName}: ${commits.get(branchName).getOrElse(List.empty).size}")
  }

  def getBranchCommits(branchName: String): List[CommitCacheEntry] = {
    logger.debug(s"Getting branch commits for ${branchName}")
    commits.get(branchName).getOrElse(List.empty[CommitCacheEntry])
  }

  def initializeWith(repository: Repository) {
    logger.debug(s"Initializing repo cache")
    val savedState = backend.loadBranchesState()
    val loadResult = repository.loadLastKnownRepoState(savedState, MaxCommitsPerBranchCount)
    loadResult.commits.foreach { branchCommits =>
      val cacheEntries = branchCommits.commits.map(partialCommitToCacheEntry)
      logger.debug(s"Adding ${cacheEntries.size} commits to ${branchCommits.branchName}")
      addCommitsToBranch(cacheEntries, branchCommits.branchName)
    }
    logger.debug(s"Cache initialized")
  }

  private def partialCommitToCacheEntry(commit: PartialCommitInfo): CommitCacheEntry = {
    CommitCacheEntry(commit.sha, commit.authorName, commit.authorEmail)
  } 

}