package com.softwaremill.codebrag.stats

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.bson.types.ObjectId
import org.joda.time.DateTime

class DailyStatisticsSpec extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  val InstanceId = new ObjectId("529d94ff300418028363ed0d").toString
  val Date = new DateTime().withDayOfMonth(1).withMonthOfYear(12).withYear(2013)
  val Counters: Map[String, Long] = Map.empty


  it should "serialize date to dd/mm/yyyy format" in {
    // given
    val stats = DailyStatistics(InstanceId, Date, Counters)

    // when
    val jsonData = stats.asJson

    // then
    val expectedJsonOutput = """{"instanceId":"529d94ff300418028363ed0d","date":"01/12/2013","counters":{}}"""
    jsonData should be(expectedJsonOutput)
  }

}
