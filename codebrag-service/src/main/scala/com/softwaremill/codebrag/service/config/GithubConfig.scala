package com.softwaremill.codebrag.service.config

import com.typesafe.config.Config

trait GithubConfig {
  def rootConfig: Config

  lazy val githubClientId: String = rootConfig.getString("github.client-id")
  lazy val githubClientSecret: String = rootConfig.getString("github.client-secret")
}
