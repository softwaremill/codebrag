package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.service.commits.jgit._
import com.softwaremill.codebrag.common.{Clock, EventBus}
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO
import com.softwaremill.codebrag.dao.branchsnapshot.BranchStateDAO
import com.softwaremill.codebrag.cache.RepositoryCache
import com.softwaremill.codebrag.service.config.CommitCacheConfig

trait CommitsModule {

  lazy val commitImportService = new CommitImportService(repoStatusDao, branchStateDao, repositoryCache, config)
  lazy val diffLoader = new JgitDiffLoader()

  def repoStatusDao: RepositoryStatusDAO
  def branchStateDao: BranchStateDAO
  def repositoryCache: RepositoryCache
  def config: CommitCacheConfig
}
