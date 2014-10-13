package com.softwaremill.codebrag.rest

import com.softwaremill.scalaval.Validation
import org.scalatra._
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.json4s._
import org.json4s.{DefaultFormats, Formats}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import java.util.Date
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.web.CodebragSpecificJSONFormats
import com.softwaremill.codebrag.web.CodebragSpecificJSONFormats.SimpleObjectIdSerializer
import com.softwaremill.codebrag.auth.RememberMeSupport

trait JsonServlet extends ScalatraServlet with CodebragJsonEndpoint with CustomErrorsHandler with CustomNotFoundErrorsHandler

trait JsonServletWithAuthentication extends JsonServlet with RememberMeSupport

trait CodebragJsonFilter extends ScalatraFilter with CodebragJsonEndpoint with CustomErrorsHandler {
  // make resolving request path identical as in ScalatraServlet
  // so that both servlet and filter can be mounted to the same url space
  // see https://groups.google.com/forum/#!topic/scalatra-user/QuQaFcUBpgc
  override def requestPath(implicit request: HttpServletRequest) = ScalatraServlet.requestPath(request)
}

trait JsonFilterWithAuthentication extends CodebragJsonFilter with RememberMeSupport

trait CodebragJsonEndpoint extends JacksonJsonSupport with JValueResult with Logging {

  protected implicit val jsonFormats = CodebragJsonEndpoint.jsonFormats

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

  def extractReqUrlParam(key: String): String = params.getOrElse(key, haltWithMissingKey(key))

  def extractReq[T: Manifest](key: String): T = (parsedBody \ key).extractOrElse[T](haltWithMissingKey(key))

  def extractOpt[T: Manifest](key: String): Option[T] = (parsedBody \ key).extractOpt[T]

  def haltWithMissingKey(key: String): Nothing = {
    halt(400, Map("error" -> s"Missing or empty element '$key' in request body"))
  }

  def applyNoCache(response: HttpServletResponse) {
    response.addHeader("Expires", Expire)
    response.addHeader("Last-Modified", Expire)
    response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
    response.addHeader("Pragma", "no-cache")
  }

  /**
   * Codebrag-specific render pipeline.
   * Converts Either and Option instances xto valid scalatra responses
   * @return
   */
  protected override def renderPipeline = renderUseCaseResult orElse renderOptionResult orElse super.renderPipeline

  private def renderUseCaseResult: PartialFunction[Any, Any] = {
    case Right(success: Any) => super.renderPipeline(Ok(success))
    case Left(error: String) => super.renderPipeline(BadRequest(Map("error" -> error)))
    case Left(errors: Validation.Errors) => super.renderPipeline(BadRequest(Map("errors" -> errors)))
  }

  private def renderOptionResult: PartialFunction[Any, Any] = {
    case Some(value: Any) => super.renderPipeline(Ok(value))
    case None => super.renderPipeline(NotFound())
  }

}

object CodebragJsonEndpoint {

  implicit val jsonFormats: Formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all ++ CodebragSpecificJSONFormats.all + SimpleObjectIdSerializer

}