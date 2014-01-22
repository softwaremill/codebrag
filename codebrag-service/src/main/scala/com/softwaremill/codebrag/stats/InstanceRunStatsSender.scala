package com.softwaremill.codebrag.stats

import com.softwaremill.codebrag.domain.InstanceSettings
import com.softwaremill.codebrag.stats.data.InstanceRunStatistics
import com.typesafe.scalalogging.slf4j.Logging

class InstanceRunStatsSender(httpSender: StatsHTTPRequestSender) extends Logging {

  def sendInstanceRunInfoImmediately(instanceSettings: InstanceSettings) {
    val statsJson = InstanceRunStatistics(instanceSettings.uniqueId).asJson
    logger.debug("Sending instance run statistics: " + statsJson)
    httpSender.sendInstanceRunInfo(statsJson)
  }

}
