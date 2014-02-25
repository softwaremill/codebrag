package com.softwaremill.codebrag.common

import com.typesafe.scalalogging.slf4j.Logging

trait Timed extends Logging {
  def timed[T](block: => T): (T, Long) = {
    val start = System.currentTimeMillis()
    val r = block
    val end = System.currentTimeMillis()
    (r, end-start)
  }

  def timedAndLogged[T](msg: String)(block: => T): T = {
    val (result, took) = timed(block)
    logger.debug(s"$msg took ${took}ms")
    result
  }
}

object Timed extends Timed
