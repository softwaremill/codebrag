package com.softwaremill.codebrag.service.events

import _root_.akka.actor.Actor
import com.softwaremill.codebrag.common.Event
import com.typesafe.scalalogging.slf4j.Logging

class EventLogger extends Actor with Logging {

  def receive = {
    case (e: Event) => logger.debug(e.toString)
  }
}
