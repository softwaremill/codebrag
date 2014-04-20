package com.softwaremill.codebrag.rest

import org.scalatra._
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.json4s._
import org.json4s.{DefaultFormats, Formats}
import javax.servlet.http.HttpServletResponse
import java.util.Date
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.web.CodebragSpecificJSONFormats


class JsonServlet extends ScalatraServlet with JacksonJsonSupport with JValueResult with Logging with CodebragErrorHandler {

  protected implicit val jsonFormats: Formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all ++ CodebragSpecificJSONFormats.all

  val Expire = new Date().toString

  before() {
    contentType = formats("json")
    applyNoCache(response)
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

  def extractOptBoolean(key: String): Option[Boolean] = (parsedBody \ key).extractOpt[Boolean]

  def haltWithMissingKey(key: String): Nothing = {
    halt(400, s"Missing or empty element '$key' in request body")
  }

  def applyNoCache(response: HttpServletResponse) {
    response.addHeader("Expires", Expire)
    response.addHeader("Last-Modified", Expire)
    response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
    response.addHeader("Pragma", "no-cache")
  }

}