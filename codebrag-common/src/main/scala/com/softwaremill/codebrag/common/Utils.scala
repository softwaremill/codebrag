package com.softwaremill.codebrag.common

import java.io.File
import java.util.Random

import com.roundeights.hasher.Implicits._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object Utils {

  val OneWeek = 7 * 24 * 3600
  val DateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  def md5(s: String): String = {
    s.md5
  }

  def sha1(s: String): String = {
    s.sha1
  }

  def sha256(password: String, salt: String): String = {
    password.salt(salt).sha256
  }

  def checkbox(s: String): Boolean = {
    s match {
      case null => false
      case _ => s.toLowerCase == "true"
    }
  }

  def format(dateTime: DateTime): String = {
    DateFormat.print(dateTime)
  }

  def randomString(length: Int) = {
    val sb = new StringBuffer()
    val r = new Random()

    for (i <- 1 to length) {
      sb.append((r.nextInt(25) + 65).toChar) // A - Z
    }

    sb.toString
  }

  def rmMinusRf(file: File) = {
    Runtime.getRuntime.exec(Array[String]("rm", "-rf", file.getAbsolutePath)).waitFor == 0
  }
}

object Joda {
    implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)
}