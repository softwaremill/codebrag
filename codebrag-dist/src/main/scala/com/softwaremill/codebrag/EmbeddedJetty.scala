package com.softwaremill.codebrag

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import java.net.InetSocketAddress

trait EmbeddedJetty {
  protected var jetty: Server = null

  def startJetty() {
    jetty = new Server(new InetSocketAddress(webServerConfig.webServerHost, webServerConfig.webServerPort))
    jetty.setHandler(prepareContext())
    jetty.start()
  }

  protected def prepareContext() = {
    val context = new WebAppContext()
    context.setContextPath("/")
    context
  }

  def stopJetty() {
    jetty.stop()
  }

  def webServerConfig: WebServerConfig
}
