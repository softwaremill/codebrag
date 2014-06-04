package com.softwaremill.codebrag.licence

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.ClockSpec
import com.softwaremill.codebrag.domain.{LicenceKey, InstanceId}
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.instance.InstanceParamsDAO
import org.mockito.Mockito._
import com.softwaremill.codebrag.dao.user.UserDAO

class LicenceServiceSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter with MockitoSugar with ClockSpec {

  var instanceParamsDao: InstanceParamsDAO = _
  var usersDao: UserDAO = _

  val ActiveUsersCount = 1

  val ValidLicence = Licence(expirationDate = clock.now.plusDays(2), maxUsers = ActiveUsersCount, companyName = "SoftwareMill")
  val ExpiredDateLicence = ValidLicence.copy(expirationDate = clock.now.minusDays(2))
  val UsersExceededLicence = ValidLicence.copy(maxUsers = 0)


  before {
    instanceParamsDao = mock[InstanceParamsDAO]
    usersDao = mock[UserDAO]
    when(usersDao.countAllActive()).thenReturn(ActiveUsersCount) // one user in Codebrag
  }

  it should "read current licence on service initialization" in {
    // when
    val service = initializeService(ValidLicence)

    // then
    service.licenceExpiryDate should be(ValidLicence.expirationDate)
    service.daysToExpire should be(ValidLicence.daysToExpire)
  }

  it should "throw exception when licence guard called and licence is expired (due to date constraint)" in {
    // given
    val service = initializeService(ExpiredDateLicence)

    // then
    intercept[LicenceExpiredException] {
      service.interruptIfLicenceExpired()
    }
  }

  it should "throw exception when licence guard called and licence is expired (due to users constraint)" in {
    // given
    val service = initializeService(UsersExceededLicence)

    // then
    intercept[LicenceExpiredException] {
      service.interruptIfLicenceExpired()
    }
  }

  it should "pass through when licence guard called and licence is valid" in {
    // given
    val service = initializeService(ValidLicence)

    // then
    service.interruptIfLicenceExpired()
  }

  it should "update licence in DB and swap current one in running app" in {
    // given
    val service = initializeService(ValidLicence)
    val newLicence = ValidLicence.copy(expirationDate = ValidLicence.expirationDate.plusDays(30), maxUsers = 50)
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
    new LicenceService(instanceId, instanceParamsDao, usersDao) {
      override protected[licence] def readCurrentLicence() = currentLicence
    }
  }

}