package com.softwaremill.codebrag.stats

import akka.actor.{ActorRef, Props, ActorSystem}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.common.Clock
import org.joda.time.Period
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent
import com.softwaremill.codebrag.service.config.CodebragStatsConfig

object StatsSendingScheduler extends Logging {

  private var actor: ActorRef = _

  def initialize(actorSystem: ActorSystem, statsAggregator: StatsAggregator, config: CodebragStatsConfig)(implicit clock: Clock) : ActorRef = {
    actor = actorSystem.actorOf(Props(new StatsSender(statsAggregator)), "dailyStatsSender")
    scheduleDaily(actorSystem, config, clock)
    actor
  }

  def scheduleDaily(actorSystem: ActorSystem, config: CodebragStatsConfig, clock: Clock) {
    import actorSystem.dispatcher
    actorSystem.scheduler.schedule(timeToScheduleFirstStatsSending(clock, config), config.statsSendInterval, actor, StatsSender.SendStatsCommand(clock.currentDateTime))
    logger.debug("Statistics calculation scheduled")
  }

  def timeToScheduleFirstStatsSending(clock: Clock, config: CodebragStatsConfig): FiniteDuration = {
    val sendPeriod = new Period().withHours(config.statsSendHour).withMinutes(config.statsSendMinute)
    val tomorrowAtSendTime = clock.currentDateTime.plusDays(1).withTimeAtStartOfDay().plus(sendPeriod)
    val configuredTimeAsMillis = tomorrowAtSendTime.getMillis - clock.currentDateTime.getMillis
    val calculatedDelay = FiniteDuration(configuredTimeAsMillis, concurrent.TimeUnit.MILLISECONDS)
    logger.debug(s"First stats sending at ${calculatedDelay.toMinutes} minutes (${calculatedDelay.toHours} hours)")
    calculatedDelay
  }
}