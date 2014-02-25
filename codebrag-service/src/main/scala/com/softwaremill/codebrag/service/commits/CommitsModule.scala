package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.service.commits.jgit._
import com.softwaremill.codebrag.common.{Clock, EventBus}
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO

trait CommitsModule {

  lazy val commitsLoader = new JgitCommitsLoader(new JgitLogConverter, repoStatusDao)

  lazy val commitImportService = new CommitImportService(
    commitsLoader,
    commitInfoDao,
    repoStatusDao,
    eventBus)(clock)

  lazy val diffLoader = new JgitDiffLoader()

  def commitInfoDao: CommitInfoDAO
  def repoStatusDao: RepositoryStatusDAO
  def eventBus: EventBus
  def clock: Clock
}
