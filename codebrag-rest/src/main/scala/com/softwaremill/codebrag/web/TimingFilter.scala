package com.softwaremill.codebrag.web

import org.scalatra.ScalatraFilter
import com.typesafe.scalalogging.slf4j.Logging

class TimingFilter extends ScalatraFilter with Logging {
  private val StartTimeKey = "TIMING_START_TIME"

  before() {
    request(StartTimeKey) = System.currentTimeMillis()
  }

  after() {
    val end = System.currentTimeMillis()
    val took = end - request(StartTimeKey).asInstanceOf[Long]
    logger.debug(s"Request took ${took}ms") // the path is in the thread name
  }
}
