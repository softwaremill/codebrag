package com.softwaremill.codebrag.usecases

import org.scalatest.{BeforeAndAfter, FlatSpec}
import com.softwaremill.codebrag.licence.{LicenceEncryptor, Licence, LicenceService}
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.common.ClockSpec
import org.mockito.Mockito._
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.user.UserDAO

class RegisterLicenceUseCaseSpec extends FlatSpec with BeforeAndAfter with MockitoSugar with ClockSpec with ShouldMatchers {

  var licenceService: LicenceService = _
  var userDao: UserDAO = _
  var useCase: RegisterLicenceUseCase = _

  before {
    licenceService = mock[LicenceService]
    userDao = mock[UserDAO]
    useCase = new RegisterLicenceUseCase(licenceService, userDao)

    when(userDao.countAllActive()).thenReturn(1)
  }

  it should "update existing licence in Codebrag" in {
    // given
    val newLicence = Licence(clock.now.plusDays(30), 50, "SoftwareMill")
    val encodedLicence = LicenceEncryptor.encode(newLicence)

    // when
    val Right(result) = useCase.execute(encodedLicence)

    // then
    verify(licenceService).updateLicence(newLicence)
    result should be(newLicence)
  }

  it should "not update existing licence when current licence has fewer users allowed than current active users count" in {
    // given
    val newLicence = Licence(clock.now.plusDays(30), 10, "SoftwareMill")
    when(userDao.countAllActive()).thenReturn(newLicence.maxUsers + 1)
    val encodedLicence = LicenceEncryptor.encode(newLicence)

    // when
    val Left(result) = useCase.execute(encodedLicence)

    // then
    verifyZeroInteractions(licenceService)
    val expectedErrors = Map("general" -> List("Too many currently active users"))
    result.fieldErrors should be(expectedErrors)
  }

  it should "not update existing licence when current licence has already expired date" in {
    // given
    val newLicence = Licence(clock.now.minusDays(1), 10, "SoftwareMill")
    when(userDao.countAllActive()).thenReturn(newLicence.maxUsers)
    val encodedLicence = LicenceEncryptor.encode(newLicence)

    // when
    val Left(result) = useCase.execute(encodedLicence)

    // then
    verifyZeroInteractions(licenceService)
    val expectedErrors = Map("general" -> List("Licence key already expired"))
    result.fieldErrors should be(expectedErrors)
  }

  it should "not update existing licence when invalid JSON key provided" in {
    // given
    val invalidLicenceString = "invalidLicenceString"

    // when
    val Left(result) = useCase.execute(invalidLicenceString)

    // then
    verifyZeroInteractions(licenceService)
    val expectedErrors = Map("licenceKey" -> List("Licence key is incorrect"))
    result.fieldErrors should be(expectedErrors)
  }

}
