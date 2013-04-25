package com.softwaremill.codebrag.dao

import pl.softwaremill.common.util.RichString
import com.softwaremill.codebrag.domain.{CommitFileInfo, CommitInfo}
import org.joda.time.DateTime
import org.bson.types.ObjectId
import ObjectIdTestUtils._

/**
 * Test utility to easily build commits.
 */
object CommitInfoBuilder {

  val EmptyListOfComments = List.empty

  val EmptyListOfFiles = List.empty

  def createRandomCommit(): CommitInfo = createRandomCommit(new ObjectId())

  def createRandomCommit(number: Long): CommitInfo = createRandomCommit(oid(number))

  def createRandomCommit(id: ObjectId): CommitInfo = {
    val sha = RichString.generateRandom(10)
    val message = RichString.generateRandom(10)
    val authorName = RichString.generateRandom(10)
    val committerName = RichString.generateRandom(10)
    val parent = RichString.generateRandom(10)
    CommitInfo(id, sha, message, authorName, committerName, new DateTime(), new DateTime(), List(parent), EmptyListOfFiles)
  }

  def createRandomCommitWithFiles(files: List[CommitFileInfo]): CommitInfo = {
    createRandomCommit().copy(files = files)
  }

}

class CommitInfoAssembler(var commit: CommitInfo) {

  def withId(newId: ObjectId) = {
    commit = commit.copy(id = newId)
    this
  }

  def withMessage(newMsg: String) = {
    commit = commit.copy(message = newMsg)
    this
  }


  def withSha(newSha: String) = {
    commit = commit.copy(sha = newSha)
    this
  }

  def withAuthorDate(newDate: DateTime) = {
    commit = commit.copy(authorDate = newDate)
    this
  }

  def withCommitDate(newDate: DateTime) = {
    commit = commit.copy(commitDate = newDate)
    this
  }

  def get = commit

}

object CommitInfoAssembler {
  def randomCommit = new CommitInfoAssembler(CommitInfoBuilder.createRandomCommit())
}
