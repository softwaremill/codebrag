package com.softwaremill.codebrag.domain

import org.joda.time.DateTime
import org.bson.types.ObjectId

case class CommitInfo(id: ObjectId, sha: String, message: String, authorName: String, committerName: String, date: DateTime, parents: List[String], files: List[CommitFileInfo]) {

  def createReviewTasksFor(users: List[ObjectId]): List[CommitToReview] = {
    // TODO: exclude commit author from review task generation as soon as we have consistent mapping between codebrag and repo users
    users.map(CommitToReview(id, _))
  }

}

object CommitInfo {
  def apply(sha: String, message: String, authorName: String, committerName: String, date: DateTime, parents: List[String], files: List[CommitFileInfo]) = {
    new CommitInfo(new ObjectId(), sha, message, authorName, committerName, date, parents, files)
  }
}

case class CommitFileInfo(filename: String, status: String, patch: String)