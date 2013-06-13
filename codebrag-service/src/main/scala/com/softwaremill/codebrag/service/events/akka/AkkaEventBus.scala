package com.softwaremill.codebrag.service.events.akka

import com.typesafe.scalalogging.slf4j.Logging
import akka.actor.ActorSystem
import com.softwaremill.codebrag.common.{EventBus, Event}

class AkkaEventBus(actorSystem: ActorSystem) extends EventBus with Logging {

  def publish(event: Event) {
    actorSystem.eventStream.publish(event)
  }
}
