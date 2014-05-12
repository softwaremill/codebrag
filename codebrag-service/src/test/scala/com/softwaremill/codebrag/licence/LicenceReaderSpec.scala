package com.softwaremill.codebrag.licence

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.instance.InstanceParamsDAO
import org.mockito.Mockito
import com.softwaremill.codebrag.domain.{InstanceId, LicenceKey}
import com.softwaremill.codebrag.common.ClockSpec
import org.bson.types.ObjectId

class LicenceReaderSpec extends FlatSpec with ShouldMatchers with MockitoSugar with ClockSpec with BeforeAndAfter {

  var _instanceParamsDao: InstanceParamsDAO = _
  val _InstanceId = instanceIdFor("20/12/2014")

  var reader: LicenceReader = _
  
  var LicenceDetails = Licence(clock.now.plusMonths(1).withTime(23, 59, 59, 999), 50, "SoftwareMill")
  var EncodedLicence = LicenceEncryptor.encode(LicenceDetails)

  before {
    _instanceParamsDao = mock[InstanceParamsDAO]
  }
  
  it should "return current licence from DB if exists" in {
    // given
    val storedLicenceKey = Some(LicenceKey(EncodedLicence).toInstanceParam)
    Mockito.when(_instanceParamsDao.findByKey(LicenceKey.Key)).thenReturn(storedLicenceKey)
    reader = instantiateLicenceReader

    // when
    val currentLicence = reader.readCurrentLicence()

    // then
    currentLicence should be(LicenceDetails)
  }

  it should "return trial licence if no proper licence exists in DB" in {
    // given
    Mockito.when(_instanceParamsDao.findByKey(LicenceKey.Key)).thenReturn(None)
    reader = instantiateLicenceReader

    // when
    val currentLicence = reader.readCurrentLicence()

    // then
    currentLicence should be(Licence.trialLicence(_InstanceId))
  }

  it should "fall back to trial licence when stored licence is not valid licence key string" in {
    // given
    val invalidLicenceStored = Some(LicenceKey("invalid_licence_key").toInstanceParam)
    Mockito.when(_instanceParamsDao.findByKey(LicenceKey.Key)).thenReturn(invalidLicenceStored)
    reader = instantiateLicenceReader

    // when
    val currentLicence = reader.readCurrentLicence()

    // then
    currentLicence should be(Licence.trialLicence(_InstanceId))
  }

  private def instantiateLicenceReader = {
    new LicenceReader {
      override def instanceId: InstanceId = _InstanceId
      override def instanceParamsDao: InstanceParamsDAO = _instanceParamsDao
    }
  }


  private def instanceIdFor(date: String) = InstanceId(new ObjectId(StringDateTestUtils.str2date(date).toDate).toString)

}