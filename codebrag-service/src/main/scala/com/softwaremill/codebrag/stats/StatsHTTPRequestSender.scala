package com.softwaremill.codebrag.stats

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.config.StatsConfig
import dispatch._

class StatsHTTPRequestSender(config: StatsConfig) extends Logging {

  def sendDailyStats(jsonData: String) {
    sendRequest(jsonData, config.dailyStatsServerUrl)
  }

  def sendInstanceRunInfo(jsonData: String) {
    sendRequest(jsonData, config.instanceRunStatsServerUrl)
  }

  private def sendRequest(data: String, statsUrl: String) {
    val request = dispatch.url(statsUrl).secure.setMethod("POST").setHeader("Content-Type", "application/json") << data
    try {
      Http(request)()
    } catch {
      case e: Exception => logger.error("Could not send statistics", e)
    }
  }

}