package com.softwaremill.codebrag.licence

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.ClockSpec

class LicenceEncryptorSpec extends FlatSpec with ShouldMatchers with ClockSpec {

  import LicenceEncryptor._

  it should "encrypt and decrypt back licence info" in {
    // given
    val date = clock.now.withTimeAtStartOfDay
    val licence = LicenceDetails(expirationDate = date, maxUsers = 50, companyName = "SoftwareMill")

    // when
    val result = decode(encode(licence))

    // then
    result should be(licence)
  }

  it should "throw exception when invalid string passed to decode licence" in {
    // given
    val invalidLicenceString = "123abc"

    // when
    intercept[InvalidLicenceKeyException] {
      decode(invalidLicenceString)
    }
  }

}
