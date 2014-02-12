package com.softwaremill.codebrag

import com.softwaremill.codebrag.service.config._
import com.softwaremill.codebrag.dao.DaoConfig

trait Config
  extends DaoConfig
  with RepositoryConfig
  with GithubConfig
  with CodebragConfig
  with EmailConfig
  with CodebragStatsConfig
