package com.softwaremill.codebrag.service.config

import pl.softwaremill.common.conf.{ MapWrapper, Configuration, Config }
import java.util

object CodebragConfiguration {

  val config: Config[String, String] = try {
    Configuration.get("application")
  } catch {
    case e: RuntimeException => new MapWrapper(new util.HashMap[String, String]())
  }

  val githubClientId      = config.get("GitHubClientId")
  val githubClientSecret  = config.get("GitHubClientSecret")
  val localGitPath        = config.get("localGitPath")
  val syncUserLogin       = config.get("syncUserLogin")
}
