package com.softwaremill.codebrag.eventstream

import akka.actor.Actor
import com.softwaremill.codebrag.domain.reactions.LikeEvent
import com.typesafe.scalalogging.slf4j.Logging

import dispatch._

class LikeEventHookPropagator(urls: List[String]) extends Actor with Logging {

  def receive = {
    case (event: LikeEvent) =>
      logger.debug(s"Sending like-event hook to subscribers...")
      for (hookUrl <- urls) {
        logger.debug(s"Sending like-hook to $hookUrl")
        val hook = url(hookUrl)
        val response = Http(hook OK as.String)
        logger.debug(s"Got response: $response")
      }
  }

}
