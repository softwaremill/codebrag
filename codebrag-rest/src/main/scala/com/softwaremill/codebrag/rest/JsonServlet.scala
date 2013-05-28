package com.softwaremill.codebrag.rest

import org.scalatra._
import org.scalatra.json.{ JValueResult, JacksonJsonSupport }
import org.json4s._
import java.io.Writer
import org.apache.commons.lang3.StringEscapeUtils._
import org.json4s.{ DefaultFormats, Formats }
import javax.servlet.http.HttpServletResponse
import java.util.Date
import com.typesafe.scalalogging.slf4j.Logging

class JsonServlet extends ScalatraServlet with JacksonJsonSupport with JValueResult with Logging {

  protected implicit val jsonFormats: Formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all

  val Expire = new Date().toString

  before() {
    contentType = formats("json")
    applyNoCache(response)
  }

  override def writeJson(json: JValue, writer: Writer) {
    (json \ "notEscapedData") match {
      case JNothing => {
        val escapedJson = json.map((x: JValue) =>
          x match {
            case JString(y) => JString(escapeHtml4(y))
            case _ => x
          }
        )
        mapper.writeValue(writer, escapedJson)
      }
      case _ => {
        mapper.writeValue(writer, json \ "notEscapedData")
      }
    }
  }

  def extractPathIntOrHalt(key: String, default: Int, errorMsg: String, constraints: Int => Boolean): Int = {
    val value = params.getAsOrElse(key, default)
    if (!constraints(value)) {
      halt(400, errorMsg)
    }
    else value
  }

  def extractNotEmptyString(key: String) = {
    val value = (parsedBody \ key).extractOrElse[String](haltWithMissingKey(key))
    if (value.trim.isEmpty) haltWithMissingKey(key)
    value
  }

  def haltWithMissingKey(key: String): Nothing = {
    halt(400, s"Missing or empty element '$key' in request body")
  }

  def applyNoCache(response: HttpServletResponse) {
    response.addHeader("Expires", Expire)
    response.addHeader("Last-Modified", Expire)
    response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
    response.addHeader("Pragma", "no-cache")
  }

  errorHandler = {
    case t: Exception => {
      logger.error("Exception during client request processing", t)
    }
    halt(500, "Internal server exception")
  }

}