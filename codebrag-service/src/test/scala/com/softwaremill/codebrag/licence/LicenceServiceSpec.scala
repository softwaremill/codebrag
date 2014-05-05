package com.softwaremill.codebrag.licence

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.ClockSpec
import com.softwaremill.codebrag.domain.{LicenceKey, InstanceId}
import com.softwaremill.codebrag.service.config.LicenceConfig
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.instance.InstanceParamsDAO
import org.mockito.Mockito._
import com.softwaremill.codebrag.dao.user.UserDAO

class LicenceServiceSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter with MockitoSugar with ClockSpec {

  var instanceParamsDao: InstanceParamsDAO = _
  var usersDao: UserDAO = _
  var config: LicenceConfig = _

  val UsersCount = 1

  val ValidDateLicence = Licence(expirationDate = clock.now.plusDays(2), maxUsers = UsersCount, companyName = "SoftwareMill")
  val ExpiredDateLicence = ValidDateLicence.copy(expirationDate = clock.now.minusDays(2))


  before {
    instanceParamsDao = mock[InstanceParamsDAO]
    usersDao = mock[UserDAO]
    config = mock[LicenceConfig]
    when(usersDao.countAll()).thenReturn(UsersCount) // one user in Codebrag
  }

  it should "read current licence on service initialization" in {
    // when
    val service = initializeService(ValidDateLicence)

    // then
    service.licenceExpiryDate should be(ValidDateLicence.expirationDate)
    service.daysToExpire should be(ValidDateLicence.daysToExpire)
  }

  it should "throw exception when licence guard called and licence is expired (due to date constraint)" in {
    // given
    val service = initializeService(ExpiredDateLicence)

    // then
    intercept[LicenceExpiredException] {
      service.interruptIfLicenceExpired
    }
  }

  it should "throw exception when licence guard called and licence is expired (due to users constraint)" in {
    // given
    val licenceWithFewerUsers = ExpiredDateLicence.copy(maxUsers = 0)
    val service = initializeService(licenceWithFewerUsers)

    // then
    intercept[LicenceExpiredException] {
      service.interruptIfLicenceExpired
    }
  }

  it should "pass through when licence guard called and licence is valid" in {
    // given
    val service = initializeService(ValidDateLicence)

    // then
    service.interruptIfLicenceExpired
  }

  it should "update licence in DB and swap current one in running app" in {
    // given
    val service = initializeService(ValidDateLicence)
    val newLicence = ValidDateLicence.copy(expirationDate = ValidDateLicence.expirationDate.plusDays(30), maxUsers = 50)
    val expectedLicenceToSave = LicenceKey(LicenceEncryptor.encode(newLicence)).toInstanceParam

    // when
    service.updateLicence(newLicence)

    // then
    service.licenceExpiryDate should be(newLicence.expirationDate)
    service.maxUsers should be(newLicence.maxUsers)
    service.companyName should be(newLicence.companyName)
    verify(instanceParamsDao).save(expectedLicenceToSave)
  }

  private def initializeService(currentLicence: Licence) = {
    val instanceId = InstanceId("123123123")
    new LicenceService(instanceId, config, instanceParamsDao, usersDao) {
      override protected[licence] def readCurrentLicence() = currentLicence
    }
  }

}