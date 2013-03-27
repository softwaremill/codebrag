package com.softwaremill.codebrag.dao

import pl.softwaremill.common.util.RichString
import com.softwaremill.codebrag.domain.CommitInfo
import org.joda.time.DateTime
import org.bson.types.ObjectId

/**
 * Test utility to easily build commits.
 */
object CommitInfoBuilder {

  def createRandomCommit(): CommitInfo = createRandomCommit(new ObjectId())

  def createRandomCommit(number: Long): CommitInfo = createRandomCommit(new ObjectId("507f191e810c19729de860e" + number))

  def createRandomCommit(id: ObjectId): CommitInfo = {
    val sha = RichString.generateRandom(10)
    val message = RichString.generateRandom(10)
    val authorName = RichString.generateRandom(10)
    val committerName = RichString.generateRandom(10)
    val parent = RichString.generateRandom(10)
    CommitInfo(id, sha, message, authorName, committerName, new DateTime(), List(parent), List.empty)
  }

}
