package com.softwaremill.codebrag.eventstream

import akka.actor.Actor
import com.ning.http.client.StringPart
import com.softwaremill.codebrag.common.Hookable
import com.softwaremill.codebrag.domain.reactions.{LikeEvent, UnlikeEvent}
import com.typesafe.scalalogging.slf4j.Logging
import dispatch._
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.FieldSerializer._
import org.json4s.ext.JodaTimeSerializers
import org.json4s.jackson.Serialization.{write => jsonWrite}
import org.json4s.mongo._
import org.json4s.{DefaultFormats, FieldSerializer}

case class HookEvent(event: Hookable, hookDate: DateTime = DateTime.now(DateTimeZone.UTC))

class EventHookPropagator(hookListeners: Map[String, List[String]]) extends Actor with Logging {

  val LikeEventSerializer = FieldSerializer[LikeEvent](ignore("clock"))
  val UnlikeEventSerializer = FieldSerializer[UnlikeEvent](ignore("clock"))

  implicit val formats =
    DefaultFormats +
    new ObjectIdSerializer() +
    LikeEventSerializer +
    UnlikeEventSerializer ++
    JodaTimeSerializers.all

  def receive = {
    case (event: Hookable) =>
      logger.debug(s"Sending ${event.hookName} to subscribers...")

      for (hookUrl <- hookListeners(event.hookName)) {
        logger.debug(s"Sending ${event.hookName} to $hookUrl")

        val json = toJSON(HookEvent(event))
        val body = new StringPart(event.hookName, json)
        val request = url(hookUrl).POST.addBodyPart(body).setHeader("Content-Type", "application/json")

        Http(request OK as.String).onComplete( (status) =>
          logger.debug(s"Got response: $status")
        )
      }
  }

  private def toJSON(hookEvent: HookEvent) = {
    jsonWrite(hookEvent)
  }

}
