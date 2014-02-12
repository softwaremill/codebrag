package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.service.commits.jgit._
import com.softwaremill.codebrag.common.{Clock, EventBus}
import com.softwaremill.codebrag.service.config.{RepositoryConfig, CodebragConfig}
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO

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
    eventBus)(clock)

  lazy val repoDataProducer = new RepoDataProducer(userDao, config)

  def commitInfoDao: CommitInfoDAO
  def repoStatusDao: RepositoryStatusDAO
  def userDao: UserDAO
  def eventBus: EventBus
  def config: CodebragConfig with RepositoryConfig
  def clock: Clock

}
