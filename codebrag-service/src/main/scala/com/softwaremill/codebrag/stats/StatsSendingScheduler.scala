package com.softwaremill.codebrag.stats

import akka.actor.{ActorRef, Props, ActorSystem}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.common.Clock
import org.joda.time.Period
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent
import com.softwaremill.codebrag.service.config.CodebragStatsConfig
import com.softwaremill.codebrag.common.scheduling.ScheduleDelaysCalculator

object StatsSendingScheduler extends Logging {

  private var actor: ActorRef = _

  def initialize(actorSystem: ActorSystem, statsAggregator: StatsAggregator, config: CodebragStatsConfig)(implicit clock: Clock) : ActorRef = {
    actor = actorSystem.actorOf(Props(new StatsSender(statsAggregator)), "dailyStatsSender")
    scheduleDaily(actorSystem, config, clock)
    actor
  }

  private def scheduleDaily(actorSystem: ActorSystem, config: CodebragStatsConfig, clock: Clock) {
    import actorSystem.dispatcher
    import scala.concurrent.duration._
    val initialDelay = ScheduleDelaysCalculator.delayToGivenTimeInMillis(config.statsSendHour, config.statsSendMinute)(clock).millis
    actorSystem.scheduler.schedule(initialDelay, config.statsSendInterval, actor, StatsSender.SendStatsCommand(clock.currentDateTime))
    logScheduleInfo(clock, initialDelay, config)
  }


  private def logScheduleInfo(clock: Clock, initialDelay: FiniteDuration, config: CodebragStatsConfig) {
    val dateAtDelay = ScheduleDelaysCalculator.dateAtDelay(clock.currentDateTime, initialDelay)
    val intervalHours = config.statsSendInterval.toHours
    val intervalMinutes = config.statsSendInterval.toMinutes
    logger.debug(s"Statistics calculation scheduled to $dateAtDelay with interval $intervalMinutes minutes ($intervalHours hours)")
  }
}