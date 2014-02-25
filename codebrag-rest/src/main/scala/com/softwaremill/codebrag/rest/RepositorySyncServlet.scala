package com.softwaremill.codebrag.rest

import com.typesafe.scalalogging.slf4j.Logging
import akka.actor.{ActorSystem, ActorRef}
import com.softwaremill.codebrag.service.updater.RepoUpdateActor
import org.scalatra.{AsyncResult, FutureSupport}
import scala.concurrent.ExecutionContext
import akka.util.Timeout
import scala.concurrent.duration._

class RepositorySyncServlet(system: ActorSystem, repoUpdateActor: ActorRef) extends JsonServlet with Logging with FutureSupport {

  protected implicit def executor: ExecutionContext = system.dispatcher

  import akka.pattern.ask

  implicit val askTimeout = Timeout(10 seconds)

  get("/") {
    new AsyncResult() {
      val is = repoUpdateActor ? RepoUpdateActor.Update(scheduleNext = false)
    }
  }
}

object RepositorySyncServlet {
  val Mapping = "/sync"
}
