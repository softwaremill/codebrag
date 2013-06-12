package com.softwaremill.codebrag.service.updater

import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.Logging

class LocalRepositoryUpdater(owner: String, repositoryName: String) extends Actor with Logging {

  def receive = {
    case LocalRepositoryUpdater.Tick => logger.info("tick!")
  }
}

object LocalRepositoryUpdater {
  val Tick = "Tick"
}