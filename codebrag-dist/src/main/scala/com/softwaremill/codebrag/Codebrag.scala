package com.softwaremill.codebrag

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.Logging
import org.eclipse.jetty.webapp.WebAppContext

object Codebrag extends App with EmbeddedJetty with Logging {
  val webServerConfig = new WebServerConfig {
    def rootConfig = ConfigFactory.load()
  }

  protected def setResourceBase(context: WebAppContext) {
    val webappDirInsideJar = context.getClass.getClassLoader.getResource("webapp").toExternalForm
    context.setWar(webappDirInsideJar)
  }

  startJetty()
  logger.info(s"Codebrag started on $jettyAddress")

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run() {
      stopJetty()
      logger.info("Codebrag stopped")
    }
  })
}
