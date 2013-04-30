package com.softwaremill.codebrag.domain.builder

import pl.softwaremill.common.util.RichString
import com.softwaremill.codebrag.domain.CommitInfo
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.CommitFileInfo

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

  def withFiles(newFiles: List[CommitFileInfo]) = {
    commit = commit.copy(files = newFiles)
    this
  }

  def withAuthorName(newName: String) = {
    commit = commit.copy(authorName = newName)
    this
  }

  def get = commit

}

object CommitInfoAssembler {

  def randomCommit = new CommitInfoAssembler(createRandomCommit)

  private def createRandomCommit = {
    val sha = RichString.generateRandom(10)
    val message = RichString.generateRandom(10)
    val authorName = RichString.generateRandom(10)
    val committerName = RichString.generateRandom(10)
    val parent = RichString.generateRandom(10)
    CommitInfo(new ObjectId, sha, message, authorName, committerName, new DateTime(), new DateTime(), List(parent), List())
  }

}
