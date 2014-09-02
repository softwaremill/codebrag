package com.softwaremill.codebrag.licence

import com.softwaremill.codebrag.common.{ClockSpec, FixtureTimeClock}
import com.softwaremill.codebrag.domain.InstanceId
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class LicenceSpec extends FlatSpec with ShouldMatchers with ClockSpec {

  val LicenceDetails = Licence(expirationDate = StringDateTestUtils.str2date("19/04/2014 23:59:59:999"), maxUsers = 50, companyName = "SoftwareMill", licenceType = LicenceType.Commercial)
  val LicenceAsJson = """{"expirationDate":"19/04/2014 23:59:59:999","maxUsers":50,"companyName":"SoftwareMill","licenceType":"Commercial"}"""

  it should "convert licence details to valid JSON string" in {
    LicenceDetails.toJsonString should be(LicenceAsJson)
  }

  it should "convert licence details JSON back to object" in {
    Licence(LicenceAsJson) should be(LicenceDetails)
  }

  it should "build trial licence using instance id provided" in {
    // given
    val oid = new ObjectId(clock.now.toDate).toString
    val instanceId = InstanceId(oid)

    // when
    val trial = Licence.trialLicence(instanceId)

    // then
    trial should be(expectedTrialLicence(clock.now))
  }

  val dateValidityTestData = List(
    Spec(clockWith("10/04/2014"), fullDaysLeft = 9, true),
    Spec(clockWith("15/04/2014"), fullDaysLeft = 4, true),
    Spec(clockWith("19/04/2014"), fullDaysLeft = 0, true),
    Spec(clockWith("20/04/2014"), fullDaysLeft = 0, false),
    Spec(clockWith("25/04/2014"), fullDaysLeft = 0, false),

    Spec(clockWith("19/04/2014 23:59:59:999"), fullDaysLeft = 0, true),
    Spec(clockWith("20/04/2014 00:00:00:000"), fullDaysLeft = 0, false)
  )

  dateValidityTestData.foreach { case Spec(clock, daysLeft, validity) =>
    val dateFormatted = clock.now.toString("dd/MM/yyyy HH:mm:ss:SSS")

    val users = LicenceDetails.maxUsers

    it should s"check licence validity for ${dateFormatted} and have result ${validity}" in {
      LicenceDetails.valid(users)(clock) should be(validity)
    }

    it should s"check days left for ${dateFormatted} and have result ${daysLeft}" in {
      LicenceDetails.daysToExpire(clock) should be(daysLeft)
    }

  }

  it should "report licence as invalid when date is ok but users count is exceeded" in {
    // given
    val exceedingUsersCount = LicenceDetails.maxUsers + 1
    implicit val clockWithNotExpiredDate = new FixtureTimeClock(LicenceDetails.expirationDate.minusDays(1).getMillis)

    // when
    val validity = LicenceDetails.valid(exceedingUsersCount)(clockWithNotExpiredDate)

    // then
    validity should be(false)
  }

  case class Spec(clock: FixtureTimeClock, fullDaysLeft: Int, valid: Boolean)

  private def clockWith(date: String) = new FixtureTimeClock(StringDateTestUtils.str2date(date).getMillis)

  private def expectedTrialLicence(date: DateTime) = {
    val expDate = clock.now.plusDays(29).withTime(23, 59, 59, 999)
    Licence(expDate, maxUsers = 999, companyName = "-", licenceType = LicenceType.Trial)
  }
}
