package com.softwaremill.codebrag.licence

import com.softwaremill.codebrag.common.Clock

case class LicenceKey(days: Int, maxUsers: Int) extends ToJsonWriter[LicenceKey]{
  
  def toLicence(companyName: String)(implicit clock: Clock) = {
    val expDate = clock.now.plusDays(days).withTime(23, 59, 59, 999)
    LicenceDetails(expDate, maxUsers, companyName, LicenceType.Commercial)
  }

  def encodeKey = LicenceEncryptor.encode(toJson)

}

object LicenceKey extends FromJsonReader{

  def decodeKey(encoded: String) = fromJson[LicenceKey](LicenceEncryptor.decode(encoded))

}