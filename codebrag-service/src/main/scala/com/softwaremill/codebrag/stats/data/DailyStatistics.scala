package com.softwaremill.codebrag.stats.data

import org.joda.time.DateTime
import net.liftweb.json.{CustomSerializer, DefaultFormats}
import net.liftweb.json.ext.{DateParser, JodaTimeSerializers}
import net.liftweb.json.JsonAST.{JNull, JString}
import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import com.softwaremill.codebrag.stats.data.StatsToJsonSerializer


case class DailyStatistics(instanceId: String, date: DateTime, counters: Map[String, Long]) extends StatsToJsonSerializer



