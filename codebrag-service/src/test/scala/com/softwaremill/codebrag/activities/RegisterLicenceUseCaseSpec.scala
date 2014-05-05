package com.softwaremill.codebrag.activities

import org.scalatest.{BeforeAndAfter, FlatSpec}
import com.softwaremill.codebrag.licence.{LicenceEncryptor, Licence, LicenceService}
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.common.ClockSpec
import org.mockito.Mockito
import org.scalatest.matchers.ShouldMatchers

class RegisterLicenceUseCaseSpec extends FlatSpec with BeforeAndAfter with MockitoSugar with ClockSpec with ShouldMatchers {

  var licenceService: LicenceService = _
  var useCase: RegisterLicenceUseCase = _

  before {
    licenceService = mock[LicenceService]
    useCase = new RegisterLicenceUseCase(licenceService)
  }

  it should "update existing licence in Codebrag" in {
    // given
    val newLicence = Licence(clock.now.plusDays(30), 50, "SoftwareMill")
    val encodedLicence = LicenceEncryptor.encode(newLicence)

    // when
    val Right(result) = useCase.execute(encodedLicence)

    // then
    Mockito.verify(licenceService).updateLicence(newLicence)
    result should be(newLicence)
  }

  it should "not update existing licence when invalid JSON key provided" in {
    // given
    val invalidLicenceString = "invalidLicenceString"

    // when
    val result = useCase.execute(invalidLicenceString)

    // then
    Mockito.verifyZeroInteractions(licenceService)
    result should be('left)
  }

}
