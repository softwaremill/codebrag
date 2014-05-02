package com.softwaremill.codebrag.licence

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.ClockSpec
import com.softwaremill.codebrag.domain.InstanceId
import com.softwaremill.codebrag.service.config.LicenceConfig
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.instance.InstanceParamsDAO

class LicenceServiceSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter with MockitoSugar with ClockSpec {

  val ValidLicence = Licence(expirationDate = clock.now.plusDays(2), maxUsers = 50, companyName = "SoftwareMill")
  val ExpiredLicence = ValidLicence.copy(expirationDate = clock.now.minusDays(2))

  it should "read current licence on service initialization" in {
    // when
    val service = initializeService(ValidLicence)

    // then
    service.licenceValid should be(ValidLicence.valid)
    service.licenceExpiryDate should be(ValidLicence.expirationDate)
    service.daysToExpire should be(ValidLicence.daysToExpire)
  }

  it should "throw exception when licence guard called and licence is expired" in {
    // given
    val service = initializeService(ExpiredLicence)

    // then
    intercept[LicenceExpiredException] {
      service.interruptIfLicenceExpired
    }
  }

  it should "pass through when licence guard called and licence is valid" in {
    // given
    val service = initializeService(ValidLicence)

    // then
    service.interruptIfLicenceExpired
  }

  private def initializeService(currentLicence: Licence) = {
    val instanceId = InstanceId("123123123")
    val config: LicenceConfig = mock[LicenceConfig]
    val instanceParamsDao: InstanceParamsDAO = mock[InstanceParamsDAO]
    new LicenceService(instanceId, config, instanceParamsDao) {
      override protected[licence] def readCurrentLicence() = currentLicence
    }
  }

}
