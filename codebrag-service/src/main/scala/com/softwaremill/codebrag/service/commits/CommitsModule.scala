package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.service.commits.jgit._
import com.softwaremill.codebrag.common.{Clock, EventBus}
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO
import com.softwaremill.codebrag.service.commits.branches.RepositoryCache

trait CommitsModule {

  lazy val commitImportService = new CommitImportService(repoStatusDao, eventBus, repositoryStateCache)(clock)
  lazy val diffLoader = new JgitDiffLoader()

  def repoStatusDao: RepositoryStatusDAO
  def eventBus: EventBus
  def clock: Clock
  def repositoryStateCache: RepositoryCache
}
