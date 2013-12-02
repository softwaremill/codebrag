package com.softwaremill.codebrag.stats

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.config.CodebragStatsConfig
import org.apache.http.impl.client.HttpClients
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.client.methods.RequestBuilder

object StatsSender extends Logging {

  def sendHttpStatsRequest(jsonData: String, config: CodebragStatsConfig): Any = {
    val client = HttpClients.createDefault()
    val reqBody = new StringEntity(jsonData, ContentType.create("application/json", "UTF-8"))
    val req = RequestBuilder.post().setUri(config.statsServerUrl).setEntity(reqBody).build()
    try {
      client.execute(req)
    } catch {
      case e: Exception => logger.error("Could not send statistics", e)
    }
  }

}