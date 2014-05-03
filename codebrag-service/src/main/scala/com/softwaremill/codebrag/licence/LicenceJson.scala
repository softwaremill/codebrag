package com.softwaremill.codebrag.licence


trait ToJsonWriter[T] {

  def toJson: String = {
    import org.json4s.jackson.Serialization.{write => writeAsJson}
    implicit val formats = Licence.JsonFormats
    writeAsJson(this)
  }

}

trait FromJsonReader {

  protected def fromJson[T: Manifest](jsonString: String) = {
    import org.json4s._
    import org.json4s.jackson.JsonMethods._
    implicit val formats = Licence.JsonFormats
    try {
      parse(jsonString).extract
    } catch {
      case e: Exception => throw new InvalidLicenceKeyException(s"Invalid JSON string provided ${jsonString}")
    }
  }

}