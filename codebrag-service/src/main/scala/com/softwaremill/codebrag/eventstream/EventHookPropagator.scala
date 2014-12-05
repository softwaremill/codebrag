package com.softwaremill.codebrag.eventstream

import akka.actor.Actor
import com.ning.http.client.StringPart
import com.softwaremill.codebrag.common.Hookable
import com.typesafe.scalalogging.slf4j.Logging
import dispatch._

class EventHookPropagator(hookListeners: Map[String, List[String]]) extends Actor with Logging {

  def toJSON(hookable: Hookable): String = s"{}"

  def receive = {
    case (event: Hookable) =>
      logger.debug(s"Sending ${event.hookName} to subscribers...")

      for (hookUrl <- hookListeners(event.hookName)) {
        logger.debug(s"Sending ${event.hookName} to $hookUrl")

        val body = new StringPart(event.hookName, toJSON(event))
        val hook = url(hookUrl).POST.addBodyPart(body).setHeader("Content-Type", "application/json")
        val response = Http(hook OK as.String)
        logger.debug(s"Got response: $response")
      }
  }

}
