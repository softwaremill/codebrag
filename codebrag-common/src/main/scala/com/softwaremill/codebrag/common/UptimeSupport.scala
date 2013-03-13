package com.softwaremill.codebrag.common

import org.joda.time.{ DateTime, Duration }

trait UptimeSupport {

  val ServerStartDate = new DateTime()

  def serverUptime() = {
    new Duration(ServerStartDate, new DateTime()).getStandardSeconds
  }

}
