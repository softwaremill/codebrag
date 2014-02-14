package com.softwaremill.codebrag.common

trait Timed {
  def timed[T](block: => T): (T, Long) = {
    val start = System.currentTimeMillis()
    val r = block
    val end = System.currentTimeMillis()
    (r, end-start)
  }
}

object Timed extends Timed
