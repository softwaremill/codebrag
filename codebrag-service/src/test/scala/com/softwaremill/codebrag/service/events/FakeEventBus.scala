package com.softwaremill.codebrag.service.events

import com.softwaremill.codebrag.common.Event

trait FakeEventBus {

  var events = new scala.collection.mutable.ListBuffer[Event]

  val eventBus = new EventBus {

    def publish(event: Event) {
      events += event
    }

    def clear() {
      events.clear()
    }

    def size() = events.size

    def getEvents = events
  }

  def getEvents = eventBus.getEvents

}
