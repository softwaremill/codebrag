package com.softwaremill.codebrag.service.events

import com.softwaremill.codebrag.common.Event

trait EventBus {

  def publish(event: Event)
}