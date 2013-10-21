package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.service.commits.jgit._
import com.softwaremill.codebrag.dao.{RepositoryStatusDAO, UserDAO, CommitInfoDAO}
import com.softwaremill.codebrag.common.EventBus
import com.softwaremill.codebrag.service.config.{RepositoryConfig, CodebragConfig}

trait CommitsModule {
  lazy val commitImportService = new CommitImportService(
    new JgitCommitsLoader(
      new JgitFacade,
      new InternalDirTree(config),
      new JgitLogConverter,
      repoStatusDao,
      repoDataProducer.getRepoTypeFromConfiguration match {
        case SvnRepoType  => new GitSvnRepoUpdater(new JgitFacade)
        case _            => new JgitRepoUpdater(new JgitFacade)
      }
    ),
    commitInfoDao,
    eventBus)

  lazy val repoDataProducer = new RepoDataProducer(userDao, config)

  def commitInfoDao: CommitInfoDAO
  def repoStatusDao: RepositoryStatusDAO
  def userDao: UserDAO
  def eventBus: EventBus
  def config: CodebragConfig with RepositoryConfig
}
