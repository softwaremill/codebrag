package com.softwaremill.codebrag.licence

import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime

object StringDateTestUtils {

  def str2date(date: String) = {
    if(date.contains(":")) {
      val formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss:SSS")
      DateTime.parse(date, formatter)
    } else {
      val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
      DateTime.parse(date, formatter).withTime(12, 00, 00, 00)
    }
  }


}
