package com.softwaremill.codebrag.stats.data

import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import org.joda.time.DateTime

import org.json4s.{CustomSerializer, DefaultFormats}
import org.json4s.ext.{DateParser, JodaTimeSerializers}
import org.json4s.JsonAST.{JNull, JString}

trait StatsToJsonSerializer {

  def asJson = {
    import org.json4s.jackson.Serialization.write
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
