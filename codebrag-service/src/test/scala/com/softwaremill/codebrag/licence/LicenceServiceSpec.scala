package com.softwaremill.codebrag.licence

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.ClockSpec
import com.softwaremill.codebrag.domain.{LicenceKey, InstanceId}
import com.softwaremill.codebrag.service.config.LicenceConfig
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.instance.InstanceParamsDAO
import org.mockito.Mockito._

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

  it should "update licence in DB and swap current one in running app" in {
    // given
    val dao = mock[InstanceParamsDAO]
    val service = initializeService(ValidLicence, dao)
    val newLicence = ValidLicence.copy(expirationDate = ValidLicence.expirationDate.plusDays(30), maxUsers = 50)
    val expectedLicenceToSave = LicenceKey(LicenceEncryptor.encode(newLicence)).toInstanceParam

    // when
    service.updateLicence(newLicence)

    // then
    service.licenceExpiryDate should be(newLicence.expirationDate)
    service.maxUsers should be(newLicence.maxUsers)
    service.companyName should be(newLicence.companyName)
    verify(dao).save(expectedLicenceToSave)
  }

  private def initializeService(currentLicence: Licence, dao: InstanceParamsDAO = mock[InstanceParamsDAO]) = {
    val instanceId = InstanceId("123123123")
    val config: LicenceConfig = mock[LicenceConfig]
    val instanceParamsDao: InstanceParamsDAO = dao
    new LicenceService(instanceId, config, instanceParamsDao) {
      override protected[licence] def readCurrentLicence() = currentLicence
    }
  }

}