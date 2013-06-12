package com.softwaremill.codebrag

import akka.actor.Props
import com.softwaremill.codebrag.service.github.jgit.DebugEventLogger
import com.softwaremill.codebrag.common.Event
import com.softwaremill.codebrag.service.actors.ActorSystemSupport

trait EventingConfiguration extends ActorSystemSupport {

  val debugLogger = actorSystem.actorOf(Props(classOf[DebugEventLogger]))
  actorSystem.eventStream.subscribe(debugLogger, classOf[Event])
}
