package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.service.commits.jgit._
import com.softwaremill.codebrag.dao.{RepositoryHeadStore, UserDAO, CommitInfoDAO}
import com.softwaremill.codebrag.common.EventBus
import com.softwaremill.codebrag.service.config.{RepositoryConfig, CodebragConfig}
import com.softwaremill.codebrag.service.commits.svnkit.{SvnFacade, SvnCommitLoader}

trait CommitsModule {
  lazy val commitImportService = new CommitImportService(
    repoDataProducer.createFromConfiguration() match {
      case Some(svn: SvnRepoData) => new SvnCommitLoader(new InternalDirTree(config), new SvnFacade(), repoHeadStore)
      case Some(git: BaseGitRepoData) => new JgitCommitsLoader(
        new JgitFacade,
        new InternalDirTree(config),
        new JgitLogConverter,
        repoHeadStore)
      case None => throw new RuntimeException("Unknown repository config");
    },
    commitInfoDao,
    eventBus)

  lazy val repoDataProducer = new RepoDataProducer(userDao, config)

  def commitInfoDao: CommitInfoDAO
  def repoHeadStore: RepositoryHeadStore
  def userDao: UserDAO
  def eventBus: EventBus
  def config: CodebragConfig with RepositoryConfig
}
