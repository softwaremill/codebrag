package com.softwaremill.codebrag.stats

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.bson.types.ObjectId
import org.joda.time.DateTime
import com.softwaremill.codebrag.stats.data.DailyStatistics

class DailyStatisticsSpec extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  val InstanceId = new ObjectId("529d94ff300418028363ed0d").toString
  val Date = new DateTime().withDayOfMonth(1).withMonthOfYear(12).withYear(2013)
  val Counters: Map[String, Int] = Map.empty
  val Version = "v1.2"


  it should "serialize date to dd/mm/yyyy format" in {
    // given
    val stats = DailyStatistics(InstanceId, Version, Date, Counters)

    // when
    val jsonData = stats.asJson

    // then
    val expectedJsonOutput = """{"instanceId":"529d94ff300418028363ed0d","appVersion":"v1.2","date":"01/12/2013","counters":{}}"""
    jsonData should be(expectedJsonOutput)
  }

}
