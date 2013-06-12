package com.softwaremill.codebrag.service.updater

import akka.actor.{Props, ActorSystem}
import scala.concurrent.duration._

object RepositoryUpdateScheduler {

  def initialize(actorSystem: ActorSystem) {
    val updaterActor = actorSystem.actorOf(Props(new LocalRepositoryUpdater("softwaremill", "codebrag")))
    import actorSystem.dispatcher

    actorSystem.scheduler.schedule(20 seconds,
      45 seconds,
      updaterActor,
      LocalRepositoryUpdater.Tick)
  }
}
