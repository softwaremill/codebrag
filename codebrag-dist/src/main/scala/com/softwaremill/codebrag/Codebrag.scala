package com.softwaremill.codebrag

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.Logging

object Codebrag extends App with EmbeddedJetty with Logging {
  val webServerConfig = new WebServerConfig {
    def rootConfig = ConfigFactory.load()
  }

  protected def getResourceBase() = {
    // The resources are in the jar
    val someRootResourceName = "index.html"
    val resourcePathWithSomeResource = Thread.currentThread().getContextClassLoader.getResource(someRootResourceName).toURI.toString
    resourcePathWithSomeResource.substring(0, resourcePathWithSomeResource.length - someRootResourceName.length)
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
