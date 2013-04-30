package com.softwaremill.codebrag.domain

import org.joda.time.DateTime
import org.bson.types.ObjectId

case class CommitInfo(id: ObjectId, sha: String, message: String, authorName: String, committerName: String, authorDate: DateTime,
                      commitDate: DateTime, parents: List[String], files: List[CommitFileInfo]) {

  def createReviewTasksFor(users: List[User]): List[CommitReviewTask] = {
    users.filterNot(_.name == authorName).map(user => CommitReviewTask(id, user.id))
  }

}

object CommitInfo {
  def apply(sha: String, message: String, authorName: String, committerName: String, authorDate: DateTime,
            commitDate: DateTime, parents: List[String], files: List[CommitFileInfo]) = {
    new CommitInfo(new ObjectId(), sha, message, authorName, committerName, authorDate, commitDate, parents, files)
  }
}

case class CommitFileInfo(filename: String, status: String, patch: String)