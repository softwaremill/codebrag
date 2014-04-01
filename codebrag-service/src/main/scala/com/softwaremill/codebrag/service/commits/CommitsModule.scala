package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.service.commits.jgit._
import com.softwaremill.codebrag.common.{Clock, EventBus}
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO
import com.softwaremill.codebrag.dao.branchsnapshot.BranchStateDAO
import com.softwaremill.codebrag.cache.BranchCommitsCache
import com.softwaremill.codebrag.service.config.CommitCacheConfig

trait CommitsModule {

  lazy val commitImportService = new CommitImportService(repoStatusDao, branchStateDao, repositoryStateCache, config)
  lazy val diffLoader = new JgitDiffLoader()

  def repoStatusDao: RepositoryStatusDAO
  def branchStateDao: BranchStateDAO
  def repositoryStateCache: BranchCommitsCache
  def config: CommitCacheConfig
}
