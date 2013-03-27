package com.softwaremill.codebrag.dao

import pl.softwaremill.common.util.RichString
import com.softwaremill.codebrag.domain.CommitInfo
import org.joda.time.DateTime

/**
 * Test utility to easily build commits.
 */
object CommitInfoBuilder {

  def createRandomCommit() = {
    val sha = RichString.generateRandom(10)
    val message = RichString.generateRandom(10)
    val authorName = RichString.generateRandom(10)
    val committerName = RichString.generateRandom(10)
    val parent = RichString.generateRandom(10)
    CommitInfo(sha, message, authorName, committerName, new DateTime(), List(parent), List.empty)
  }
}
