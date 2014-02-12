package com.softwaremill.codebrag.stats.data

import org.joda.time.DateTime

case class DailyStatistics(instanceId: String, date: DateTime, counters: Map[String, Int]) extends StatsToJsonSerializer



