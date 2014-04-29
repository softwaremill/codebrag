package com.softwaremill.codebrag.licence

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.FixtureTimeClock
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.{InstanceId, InstanceSettings}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.config.LicenceConfig
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.joda.time.format.DateTimeFormat

class LicenceServiceSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter with MockitoSugar {

  var instanceId = instanceIdFor("10/04/2014")
  var config: LicenceConfig = mock[LicenceConfig]

  before {
    when(config.expiresInDays).thenReturn(10) // 19/04/2014 is last valid date
  }

  val testData = List(
    Spec(clockWith("10/04/2014"), fullDaysLeft = 9, true),
    Spec(clockWith("15/04/2014"), fullDaysLeft = 4, true),
    Spec(clockWith("19/04/2014"), fullDaysLeft = 0, true),
    Spec(clockWith("20/04/2014"), fullDaysLeft = 0, false),
    Spec(clockWith("25/04/2014"), fullDaysLeft = 0, false),

    Spec(clockWith("19/04/2014 23:59"), fullDaysLeft = 0, true),
    Spec(clockWith("20/04/2014 00:00"), fullDaysLeft = 0, false)
  )

  testData.foreach { case Spec(clock, daysLeft, validity) =>
    val dateFormatted = clock.now.toString("dd/MM/yyyy HH:mm")

    it should s"check licence validity for ${dateFormatted} and have result ${validity}" in {
      new LicenceService(instanceId, config, clock).licenceValid should be(validity)
    }

    it should s"check days left for ${dateFormatted} and have result ${daysLeft}" in {
      new LicenceService(instanceId, config, clock).daysToExpire should be(daysLeft)
    }

  }

  case class Spec(clock: FixtureTimeClock, fullDaysLeft: Int, valid: Boolean)

  private def str2date(date: String) = {
    if(date.contains(":")) {
      val formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm")
      DateTime.parse(date, formatter)
    } else {
      val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
      DateTime.parse(date, formatter).withTime(12, 00, 00, 00)
    }
  }

  private def clockWith(date: String) = new FixtureTimeClock(str2date(date).getMillis)

  private def instanceIdFor(date: String) = InstanceId(new ObjectId(str2date(date).toDate).toString)

}
