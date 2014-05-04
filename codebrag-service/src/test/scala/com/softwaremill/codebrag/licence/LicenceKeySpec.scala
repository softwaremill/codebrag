package com.softwaremill.codebrag.licence

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.ClockSpec

class LicenceKeySpec extends FlatSpec with ShouldMatchers with ClockSpec {

  it should "build licence using licence key" in {
    // given
    val licenceKey = LicenceKey(days = 30, maxUsers = 50)

    // when
    val licence = licenceKey.toLicence(companyName = "SoftwareMill")

    // then
    val expectedLicence = Licence(clock.now.plusDays(30).withTime(23, 59, 59, 999), 50, "SoftwareMill", LicenceType.Commercial)
    licence should be(expectedLicence)
  }

  it should "encode and decode it back" in {
    // given
    val licenceKey = LicenceKey(days = 30, maxUsers = 50)

    // when
    val result = LicenceKey.decodeKey(licenceKey.encodeKey)

    // then
    result should be(licenceKey)
  }

}
