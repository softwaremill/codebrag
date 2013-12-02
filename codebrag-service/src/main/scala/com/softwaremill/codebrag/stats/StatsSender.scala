package com.softwaremill.codebrag.stats

import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.Logging
import org.joda.time.DateTime

class StatsSender(statsAggregator: StatsAggregator) extends Actor with Logging {

  def receive = {
    case StatsSender.SendStatsCommand(currentDate: DateTime) => {
      statsAggregator.getStatsForPreviousDayOf(currentDate.plusDays(1)).right.foreach { stats =>
        logger.debug(s"### Sending stats $stats")
      }
    }
  }

}

object StatsSender {
  case class SendStatsCommand(currentTime: DateTime)
}