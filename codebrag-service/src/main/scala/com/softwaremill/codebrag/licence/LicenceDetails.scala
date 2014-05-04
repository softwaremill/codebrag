package com.softwaremill.codebrag.licence

import org.joda.time.{Days, DateTime}
import com.softwaremill.codebrag.common.Clock
import com.typesafe.scalalogging.slf4j.Logging
import org.json4s.ext.EnumNameSerializer
import com.softwaremill.codebrag.licence.LicenceType.LicenceType
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}
import org.joda.time.format.DateTimeFormat

case class LicenceDetails(expirationDate: DateTime, maxUsers: Int, companyName: String, licenceType: LicenceType = LicenceType.Commercial) extends Logging with ToJsonWriter[LicenceDetails] {

  def valid(implicit clock: Clock) = !expirationDate.isBefore(clock.now)

  def daysToExpire(implicit clock: Clock) = {
    val days = Days.daysBetween(clock.now.withTimeAtStartOfDay(), expirationDate).getDays
    if(days < 0) 0 else days
  }

  def encodeLicence = {
    val json = toJson
    LicenceEncryptor.encode(json)
  }

}

object LicenceDetails extends FromJsonReader {

  implicit val JsonFormats = org.json4s.DefaultFormats + new EnumNameSerializer(LicenceType) + FullDateSerializer

  def decodeLicence(encoded: String) = fromJson[LicenceDetails](LicenceEncryptor.decode(encoded))

  private val Formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss:SSS")

  private object FullDateSerializer extends CustomSerializer[DateTime](format => ( {
    case JString(d) => DateTime.parse(d, Formatter)
    case JNull => null
  }, {
    case d: DateTime => JString(Formatter.print(d))
  }
    ))
}

