package com.softwaremill.codebrag.cache

import com.softwaremill.codebrag.service.config.CommitCacheConfig
import java.util.concurrent.ConcurrentHashMap
import com.softwaremill.codebrag.repository.Repository
import scala.collection.JavaConversions._
import com.softwaremill.codebrag.domain.MultibranchLoadCommitsResult

class RepositoriesCache(backend: PersistentBackendForCache, config: CommitCacheConfig) {

  private val reposCacheMap = new ConcurrentHashMap[String, RepositoryCache]

  def initialize(repos: Seq[Repository]) {
    repos.foreach { repo =>
      val repoCache = new RepositoryCache(repo, backend, config)
      repoCache.initialize()
      reposCacheMap.put(repo.repoName, repoCache)
    }
  }

  def repoNames = reposCacheMap.keySet().toList.sorted

  def hasRepo(repoName: String) = reposCacheMap.containsKey(repoName)

  def getRepo(repoName: String) = reposCacheMap.getOrElse(repoName, throw new IllegalArgumentException(s"Cannot find repository $repoName"))

  def addCommitsToRepo(repoName: String, commits: MultibranchLoadCommitsResult) = getRepo(repoName).addCommits(commits)

  def getBranchCommits(repoName: String, branch: String) = getRepo(repoName).getBranchCommits(branch)

  def getCheckedOutBranchShortName(repoName: String) = getRepo(repoName).getCheckedOutBranchShortName

}
