package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.service.commits.jgit._
import com.softwaremill.codebrag.common.{Clock, EventBus}
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO
import com.softwaremill.codebrag.dao.branchsnapshot.BranchStateDAO
import com.softwaremill.codebrag.cache.BranchCommitsCache

trait CommitsModule {

  lazy val commitImportService = new CommitImportService(repoStatusDao, branchStateDao, repositoryStateCache)
  lazy val diffLoader = new JgitDiffLoader()

  def repoStatusDao: RepositoryStatusDAO
  def branchStateDao: BranchStateDAO
  def repositoryStateCache: BranchCommitsCache
}
