package com.softwaremill.codebrag

import com.softwaremill.codebrag.service.config._
import com.softwaremill.codebrag.dao.DaoConfig

trait AllConfig
  extends DaoConfig
  with MultiRepoConfig
  with CodebragConfig
  with EmailConfig
  with StatsConfig
  with CommitCacheConfig
  with ReviewProcessConfig
  with HooksConfig
