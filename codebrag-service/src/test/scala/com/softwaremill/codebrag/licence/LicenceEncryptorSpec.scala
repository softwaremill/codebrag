package com.softwaremill.codebrag.licence

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.ClockSpec

class LicenceEncryptorSpec extends FlatSpec with ShouldMatchers {

  import LicenceEncryptor._

  it should "encrypt and decrypt back licence info" in {
    // given
    val source = "text to decode"

    // when
    val result = decode(encode(source))

    // then
    result should be(source)
  }

  it should "throw exception when invalid string passed to decode licence" in {
    // given
    val invalidEncodedString = "123abc"

    // when
    intercept[InvalidLicenceKeyException] {
      decode(invalidEncodedString)
    }
  }

}
