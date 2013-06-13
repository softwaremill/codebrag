package com.softwaremill.codebrag.common


trait EventBus {

  def publish(event: Event)
}