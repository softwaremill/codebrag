package com.softwaremill.codebrag

import com.typesafe.config.Config
import com.softwaremill.codebrag.common.config.ConfigWithDefault

trait WebServerConfig extends ConfigWithDefault {
  def rootConfig: Config

  lazy val webServerHost: String = getString("codebrag.web-server-host", "0.0.0.0")
  lazy val webServerPort: Int = getInt("codebrag.web-server-port", 8080)
}
