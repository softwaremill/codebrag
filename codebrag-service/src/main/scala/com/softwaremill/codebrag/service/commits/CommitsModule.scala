package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.service.commits.jgit._
import com.softwaremill.codebrag.dao.{RepositoryStatusDAO, UserDAO, CommitInfoDAO}
import com.softwaremill.codebrag.common.{Clock, EventBus}
import com.softwaremill.codebrag.service.config.{RepositoryConfig, CodebragConfig}

trait CommitsModule {

  lazy val commitsLoader = new JgitCommitsLoader(new JgitLogConverter, repoStatusDao)

  lazy val commitImportService = new CommitImportService(
    commitsLoader,
    commitInfoDao,
    eventBus)(clock)

  def commitInfoDao: CommitInfoDAO
  def repoStatusDao: RepositoryStatusDAO
  def userDao: UserDAO
  def eventBus: EventBus
  def config: CodebragConfig with RepositoryConfig
  def clock: Clock

}
