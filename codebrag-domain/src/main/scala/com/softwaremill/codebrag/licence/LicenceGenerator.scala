package com.softwaremill.codebrag.licence

import org.joda.time.DateTime

class LicenceGenerator {

  def generateLicence(date: DateTime, users: Int, companyName: String): String = {
    val licence = Licence(date.withTime(23, 59, 59, 999), users, companyName)
    LicenceEncryptor.encode(licence)
  }

}
