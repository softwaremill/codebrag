package com.softwaremill.codebrag

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.Logging

object Codebrag extends App with EmbeddedJetty with Logging {
  def webServerConfig = new WebServerConfig {
    def rootConfig = ConfigFactory.load()
  }

  startJetty()
  logger.info(s"Codebrag started on $jettyAddress")
}
