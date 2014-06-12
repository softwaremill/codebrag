package com.softwaremill.codebrag.domain.builder

import com.softwaremill.codebrag.domain.CommitInfo
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.{RealTimeClock, Utils}

class CommitInfoAssembler(var commit: CommitInfo) {

  def withRepo(newRepoName: String) = {
    commit = commit.copy(repoName = newRepoName)
    this
  }

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

  def withAuthorName(newName: String) = {
    commit = commit.copy(authorName = newName)
    this
  }

  def withAuthorEmail(newAuthorEmail: String) = {
    commit = commit.copy(authorEmail = newAuthorEmail)
    this
  }

  def get = commit

}

object CommitInfoAssembler {

  def randomCommits(count: Int) = { List.fill(count)(createRandomCommit) }

  def randomCommit = new CommitInfoAssembler(createRandomCommit)

  private def createRandomCommit = {
    val sha = Utils.randomString(10)
    val repoName  = Utils.randomString(10)
    val message = Utils.randomString(10)
    val authorName = Utils.randomString(10)
    val authorEmail = Utils.randomString(10)
    val committerName = Utils.randomString(10)
    val committerEmail = Utils.randomString(10)
    val parent = Utils.randomString(10)
    CommitInfo(new ObjectId, repoName, sha, message, authorName, authorEmail, committerName, committerEmail,
      RealTimeClock.nowUtc, RealTimeClock.nowUtc, List(parent))
  }

}
