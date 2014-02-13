package com.softwaremill.codebrag

import com.typesafe.config.Config

trait WebServerConfig {
  def rootConfig: CodebragConfig

  lazy val webServerHost: String = rootConfig.getString("web-server.host")
  lazy val webServerPort: Int = rootConfig.getInt("web-server.port")
}
