package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.service.commits.jgit._
import com.softwaremill.codebrag.dao.{RepositoryHeadStore, UserDAO, CommitInfoDAO}
import com.softwaremill.codebrag.common.EventBus
import com.softwaremill.codebrag.service.config.{RepositoryConfig, CodebragConfig}

trait CommitsModule {
  lazy val commitImportService = new CommitImportService(
    new JgitCommitsLoader(
      new JgitFacade,
      new InternalDirTree(config),
      new JgitLogConverter,
      repoHeadStore,
      repoDataProducer.createFromConfiguration() match {
        case Some(svn: SvnRepoData) => new GitSvnRepoUpdater(new JgitFacade, repoHeadStore)
        case Some(git: RepoData) => new JgitRepoUpdater(new JgitFacade, repoHeadStore)
        case None => throw new RuntimeException("Unknown repository config");
      }
    ),
    commitInfoDao,
    eventBus)

  lazy val repoDataProducer = new RepoDataProducer(userDao, config)

  def commitInfoDao: CommitInfoDAO
  def repoHeadStore: RepositoryHeadStore
  def userDao: UserDAO
  def eventBus: EventBus
  def config: CodebragConfig with RepositoryConfig
}
