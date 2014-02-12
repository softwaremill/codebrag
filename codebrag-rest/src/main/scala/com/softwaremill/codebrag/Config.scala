package com.softwaremill.codebrag

import com.softwaremill.codebrag.dao.mongo.MongoConfig
import com.softwaremill.codebrag.service.config._

trait Config
  extends MongoConfig
  with RepositoryConfig
  with GithubConfig
  with CodebragConfig
  with EmailConfig
  with CodebragStatsConfig
