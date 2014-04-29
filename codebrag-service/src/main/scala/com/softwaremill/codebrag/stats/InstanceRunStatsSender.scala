package com.softwaremill.codebrag.stats

import com.softwaremill.codebrag.domain.{InstanceId, InstanceSettings}
import com.softwaremill.codebrag.stats.data.InstanceRunStatistics
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.config.StatsConfig

class InstanceRunStatsSender(httpSender: StatsHTTPRequestSender) extends Logging {

  def sendInstanceRunInfoImmediately(instanceId: InstanceId, appVersion: String) {
    val statsJson = InstanceRunStatistics(instanceId.value, appVersion).asJson
    logger.debug("Sending instance run statistics: " + statsJson)
    httpSender.sendInstanceRunInfo(statsJson)
  }

}
