package com.softwaremill.codebrag.stats

import akka.actor.{ActorRef, Props, ActorSystem}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.common.Clock
import scala.concurrent.duration.FiniteDuration
import com.softwaremill.codebrag.service.config.StatsConfig
import com.softwaremill.codebrag.common.scheduling.ScheduleDelaysCalculator

object StatsSendingScheduler extends Logging {

  private var actor: ActorRef = _

  def initialize(actorSystem: ActorSystem, statsAggregator: StatsAggregator, httpSender: StatsHTTPRequestSender, config: StatsConfig)(implicit clock: Clock) : ActorRef = {
    actor = actorSystem.actorOf(Props(new StatsSenderActor(statsAggregator, httpSender)), "dailyStatsSender")
    scheduleDaily(actorSystem, config, clock)
    actor
  }

  private def scheduleDaily(actorSystem: ActorSystem, config: StatsConfig, clock: Clock) {
    import actorSystem.dispatcher
    import scala.concurrent.duration._
    val initialDelay = ScheduleDelaysCalculator.delayToGivenTimeInMillis(config.statsSendHour, config.statsSendMinute)(clock).millis
    actorSystem.scheduler.schedule(initialDelay, config.statsSendInterval, actor, StatsSenderActor.SendStatsCommand(clock))
    logScheduleInfo(clock, initialDelay, config)
  }


  private def logScheduleInfo(clock: Clock, initialDelay: FiniteDuration, config: StatsConfig) {
    val dateAtDelay = ScheduleDelaysCalculator.dateAtDelay(clock.now, initialDelay)
    val intervalHours = config.statsSendInterval.toHours
    val intervalMinutes = config.statsSendInterval.toMinutes
    logger.debug(s"Statistics calculation scheduled to $dateAtDelay with interval $intervalMinutes minutes ($intervalHours hours)")
  }
}