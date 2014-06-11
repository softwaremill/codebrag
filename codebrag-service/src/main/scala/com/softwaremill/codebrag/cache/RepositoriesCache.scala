package com.softwaremill.codebrag.cache

import com.softwaremill.codebrag.service.config.CommitCacheConfig
import java.util.concurrent.ConcurrentHashMap
import com.softwaremill.codebrag.repository.Repository
import scala.collection.JavaConversions._

class RepositoriesCache(backend: PersistentBackendForCache, config: CommitCacheConfig) {

  private val reposCacheMap = new ConcurrentHashMap[String, RepositoryCache]

  def initialize(repos: Seq[Repository]) {
    repos.foreach { repo =>
      val repoCache = new RepositoryCache(repo, backend, config)
      repoCache.initialize()
      reposCacheMap.put(repo.repoName, repoCache)
    }
  }

  def getRepo(repoName: String) = reposCacheMap.getOrElse(repoName, throw new IllegalArgumentException(s"Cannot find repository $repoName"))

}
