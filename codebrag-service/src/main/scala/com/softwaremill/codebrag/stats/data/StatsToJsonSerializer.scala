package com.softwaremill.codebrag.stats.data

import net.liftweb.json.{CustomSerializer, DefaultFormats}
import net.liftweb.json.ext.{DateParser, JodaTimeSerializers}
import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import org.joda.time.DateTime
import net.liftweb.json.JsonAST.{JNull, JString}

trait StatsToJsonSerializer {

  def asJson = {
    import net.liftweb.json.Serialization.write

    implicit val formats = DefaultFormats ++ JodaTimeSerializers.all + new DateOnlySerializer(DateTimeFormat.forPattern("dd/MM/yyyy"))
    write(this)
  }

  private case class DateOnlySerializer(formatter: DateTimeFormatter) extends CustomSerializer[DateTime](format => (
    {
      case JString(s) => new DateTime(DateParser.parse(s, format))
      case JNull => null
    },
    {
      case d: DateTime => JString(formatter.print(d))
    })
  )

}
