package com.softwaremill.codebrag.activities

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

    when(userDao.countAll()).thenReturn(1)
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

  it should "not update existing licence when current licence has fewer users allowed than current users count" in {
    // given
    val newLicence = Licence(clock.now.plusDays(30), 10, "SoftwareMill")
    when(userDao.countAll()).thenReturn(newLicence.maxUsers + 1)
    val encodedLicence = LicenceEncryptor.encode(newLicence)

    // when
    val result = useCase.execute(encodedLicence)

    // then
    verifyZeroInteractions(licenceService)
    result should be('left)
  }

  it should "not update existing licence when invalid JSON key provided" in {
    // given
    val invalidLicenceString = "invalidLicenceString"

    // when
    val result = useCase.execute(invalidLicenceString)

    // then
    verifyZeroInteractions(licenceService)
    result should be('left)
  }

}
