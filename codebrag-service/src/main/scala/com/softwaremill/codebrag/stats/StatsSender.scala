package com.softwaremill.codebrag.stats

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.config.CodebragStatsConfig
import dispatch._

object StatsSender extends Logging {

  def sendHttpStatsRequest(jsonData: String, config: CodebragStatsConfig) {
    val svc = dispatch.url(config.statsServerUrl).setMethod("POST").setHeader("Content-Type", "application/json") << jsonData
    try {
      Http(svc)()
    } catch {
      case e: Exception => logger.error("Could not send statistics", e)
    }
  }

}