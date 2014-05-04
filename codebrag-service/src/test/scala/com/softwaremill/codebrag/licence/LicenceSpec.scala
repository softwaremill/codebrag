package com.softwaremill.codebrag.licence

import org.scalatest.FlatSpec
import org.joda.time.DateTime
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.InstanceId
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.{FixtureTimeClock, ClockSpec}

class LicenceSpec extends FlatSpec with ShouldMatchers with ClockSpec {

  val LicenceDetails = Licence(expirationDate = StringDateTestUtils.str2date("19/04/2014 23:59:59:999"), maxUsers = 50, companyName = "SoftwareMill", licenceType = LicenceType.Commercial)

  it should "encode licence and decode it back" in {
    // when
    val encoded = LicenceDetails.encodeLicence
    val decoded = Licence.decodeLicence(encoded)
    
    // then
    decoded should be(LicenceDetails)
  }

  it should "build trial licence using instance id provided" in {
    // given
    val oid = new ObjectId(clock.now.toDate).toString
    val instanceId = InstanceId(oid)

    // when
    val trial = TrialLicenceBuilder.generate(instanceId, days = 15)

    // then
    trial should be(expectedTrialLicence(clock.now))
  }

  val checkValidityTestData = List(
    Spec(clockWith("10/04/2014"), fullDaysLeft = 9, true),
    Spec(clockWith("15/04/2014"), fullDaysLeft = 4, true),
    Spec(clockWith("19/04/2014"), fullDaysLeft = 0, true),
    Spec(clockWith("20/04/2014"), fullDaysLeft = 0, false),
    Spec(clockWith("25/04/2014"), fullDaysLeft = 0, false),

    Spec(clockWith("19/04/2014 23:59:59:999"), fullDaysLeft = 0, true),
    Spec(clockWith("20/04/2014 00:00:00:000"), fullDaysLeft = 0, false)
  )

  checkValidityTestData.foreach { case Spec(clock, daysLeft, validity) =>
    val dateFormatted = clock.now.toString("dd/MM/yyyy HH:mm:ss:SSS")

    it should s"check licence validity for ${dateFormatted} and have result ${validity}" in {
      LicenceDetails.valid(clock) should be(validity)
    }

    it should s"check days left for ${dateFormatted} and have result ${daysLeft}" in {
      LicenceDetails.daysToExpire(clock) should be(daysLeft)
    }

  }

  case class Spec(clock: FixtureTimeClock, fullDaysLeft: Int, valid: Boolean)

  private def clockWith(date: String) = new FixtureTimeClock(StringDateTestUtils.str2date(date).getMillis)

  private def expectedTrialLicence(date: DateTime) = {
    val expDate = clock.now.plusDays(14).withTime(23, 59, 59, 999)
    Licence(expDate, maxUsers = 0, companyName = "-", licenceType = LicenceType.Trial)
  }
}
