package com.softwaremill.codebrag.repository.config

import com.typesafe.scalalogging.slf4j.Logging
import com.typesafe.config.Config
import java.io.File

abstract class RepoConfig extends Logging {

  protected def repositoryConfig: Config

  lazy val repoLocation = repositoryConfig.getString("location")
  lazy val repoName = repoLocation.split(File.separator).last

}
