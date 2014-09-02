package com.softwaremill.codebrag.licence

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}
import org.json4s.ext.EnumNameSerializer

object LicenceAsJson {

  private val Formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss:SSS")
  implicit val JsonFormats = org.json4s.DefaultFormats + FullDateTimeSerializer + new EnumNameSerializer(LicenceType)

  def toJson(licence: Licence) = {
    import org.json4s.jackson.Serialization.write
    write(licence)
  }

  def fromJson(string: String): Licence = {
    import org.json4s._
    import org.json4s.jackson.JsonMethods._
    try {
      parse(string).extract[Licence]
    } catch {
      case e: Exception => throw new InvalidLicenceKeyException(s"Invalid licence key provided ${string}")
    }
  }

  private object FullDateTimeSerializer extends CustomSerializer[DateTime](format => ( {
    case JString(d) => DateTime.parse(d, Formatter)
    case JNull => null
    }, {
    case d: DateTime => JString(Formatter.print(d))
    }
  ))

}
