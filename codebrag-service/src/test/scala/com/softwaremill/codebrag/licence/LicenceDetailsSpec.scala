package com.softwaremill.codebrag.licence

import org.scalatest.FlatSpec
import org.joda.time.DateTime
import org.scalatest.matchers.ShouldMatchers

class LicenceDetailsSpec extends FlatSpec with ShouldMatchers {

  val Date = DateTime.now().withDate(2014, 4, 29)
  val Licence = LicenceDetails(expirationDate = Date.withTimeAtStartOfDay(), maxUsers = 50, companyName = "SoftwareMill")
  val LicenceAsJson = """{"expirationDate":"29/04/2014","maxUsers":50,"companyName":"SoftwareMill"}"""

  it should "convert licence details to valid JSON string" in {
    Licence.toJson should be(LicenceAsJson)
  }

  it should "convert licence details JSON back to object" in {
    LicenceDetails(LicenceAsJson) should be(Licence)
  }

}
