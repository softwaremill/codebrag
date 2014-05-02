package com.softwaremill.codebrag.licence

import org.joda.time.{Days, DateTime}
import org.joda.time.format.DateTimeFormat
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}
import com.softwaremill.codebrag.domain.InstanceId
import com.softwaremill.codebrag.common.Clock
import com.typesafe.scalalogging.slf4j.Logging
import org.json4s.ext.EnumNameSerializer
import com.softwaremill.codebrag.licence.LicenceType.LicenceType

case class Licence(expirationDate: DateTime, maxUsers: Int, companyName: String, licenceType: LicenceType = LicenceType.Commercial) extends Logging {

  def valid(implicit clock: Clock) = !expirationDate.isBefore(clock.now)

  def daysToExpire(implicit clock: Clock) = {
    val days = Days.daysBetween(clock.now.withTimeAtStartOfDay(), expirationDate).getDays
    if(days < 0) 0 else days
  }

  def toJson = {
    import org.json4s.jackson.Serialization.{write => writeAsJson}
    implicit val formats = Licence.JsonFormats
    writeAsJson(this)
  }

}

object Licence {

  private val Formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
  implicit val JsonFormats = org.json4s.DefaultFormats + DateOnlySerializer + new EnumNameSerializer(LicenceType)

  def apply(decodedKey: String) = {
    import org.json4s._
    import org.json4s.jackson.JsonMethods._
    try {
      val json = parse(decodedKey)
      val decoded = json.extract[Licence]
      // set end of the day as expiration date
      decoded.copy(expirationDate = decoded.expirationDate.withTime(23, 59, 59, 999))
    } catch {
      case e: Exception => throw new InvalidLicenceKeyException(s"Invalid licence key provided ${decodedKey}")
    }
  }

  def trialLicence(instanceId: InstanceId, days: Int) = {
    val instanceCreationDate = new DateTime(instanceId.creationTime).withTimeAtStartOfDay()
    val licenceExpiryDate = instanceCreationDate.plusDays(days - 1).withTime(23, 59, 59, 999)
    Licence(licenceExpiryDate, 0, "-", LicenceType.Trial)
  }

  private object DateOnlySerializer extends CustomSerializer[DateTime](format => ( {
    case JString(d) => DateTime.parse(d, Formatter)
    case JNull => null
    }, {
      case d: DateTime => JString(Formatter.print(d))
    }
  ))

}


object LicenceType extends Enumeration {
  type LicenceType = Value
  val Trial, Commercial = Value
}