package com.softwaremill.codebrag.licence

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}

case class LicenceDetails(expirationDate: DateTime, maxUsers: Int, companyName: String) {

  def toJson = {
    import org.json4s.jackson.Serialization.{write => writeAsJson}
    implicit val formats = LicenceDetails.JsonFormats
    writeAsJson(this)
  }

}

object LicenceDetails {

  private val Formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
  implicit val JsonFormats = org.json4s.DefaultFormats + DateOnlySerializer

  def apply(jsonString: String) = {
    import org.json4s._
    import org.json4s.jackson.JsonMethods._
    val json = parse(jsonString)
    json.extract[LicenceDetails]
  }

  private object DateOnlySerializer extends CustomSerializer[DateTime](format => ( {
    case JString(d) => DateTime.parse(d, Formatter)
    case JNull => null
    }, {
      case d: DateTime => JString(Formatter.print(d))
    }
  ))

}


