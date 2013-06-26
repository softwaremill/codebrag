package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.service.commits.jgit._
import com.softwaremill.codebrag.dao.{UserDAO, CommitInfoDAO}
import com.softwaremill.codebrag.common.EventBus
import com.softwaremill.codebrag.service.config.CodebragConfig

trait CommitsModule {
  lazy val commitImportService = new CommitImportService(
    new JgitCommitsLoader(
      new JgitFacade,
      new InternalGitDirTree(config),
      new JgitLogConverter,
      commitInfoDao),
    commitInfoDao,
    eventBus)

  lazy val importerFactory = new JgitGitHubCommitImportServiceFactory(commitInfoDao, userDao, eventBus, config)

  def commitInfoDao: CommitInfoDAO
  def userDao: UserDAO
  def eventBus: EventBus
  def config: CodebragConfig
}
