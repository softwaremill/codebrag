package com.softwaremill.codebrag.eventstream

import com.typesafe.scalalogging.slf4j.Logging
import akka.actor.Actor
import com.softwaremill.codebrag.common.StatisticEvent
import com.softwaremill.codebrag.dao.eventstream.EventDao

class StatisticEventsCollector(val eventDao: EventDao) extends Actor with Logging {

  def receive = {
    case (event: StatisticEvent) => eventDao.storeEvent(event)
  }

}