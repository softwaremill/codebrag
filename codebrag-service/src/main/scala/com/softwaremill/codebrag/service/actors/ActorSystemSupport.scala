package com.softwaremill.codebrag.service.actors

import akka.actor.ActorSystem
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}

trait ActorSystemSupport {
  val actorSystem = ActorSystem.create("codebrag", ConfigFactory.load("akka", ConfigParseOptions.defaults.setAllowMissing(false), ConfigResolveOptions.defaults))

}
