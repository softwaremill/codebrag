package com.softwaremill.codebrag.licence

import com.softwaremill.codebrag.common.Clock

case class LicenceKeyToRegister(days: Int, maxUsers: Int) extends ToJsonWriter[LicenceKeyToRegister]{
  
  def toLicence(companyName: String)(implicit clock: Clock) = {
    val expDate = clock.now.plusDays(days).withTime(23, 59, 59, 999)
    Licence(expDate, maxUsers, companyName, LicenceType.Commercial)
  }

  def encodeKey = LicenceEncryptor.encode(toJson)

}

object LicenceKeyToRegister extends FromJsonReader{

  def decodeKey(encoded: String) = fromJson[LicenceKeyToRegister](LicenceEncryptor.decode(encoded))

}