package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.service.commits.jgit._
import com.softwaremill.codebrag.common.{Clock, EventBus}
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO
import com.softwaremill.codebrag.service.commits.branches.RepositoryCache
import com.softwaremill.codebrag.dao.branchsnapshot.BranchStateDAO

trait CommitsModule {

  lazy val commitImportService = new CommitImportService(repoStatusDao, branchStateDao, eventBus, repositoryStateCache)(clock)
  lazy val diffLoader = new JgitDiffLoader()

  def repoStatusDao: RepositoryStatusDAO
  def branchStateDao: BranchStateDAO
  def eventBus: EventBus
  def clock: Clock
  def repositoryStateCache: RepositoryCache
}
