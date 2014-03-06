package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId
import org.joda.time.DateTime

case class PartialCommitInfo(id: ObjectId, sha: String, message: String, authorName: String, authorEmail: String, date: DateTime) {

  // temp method to convert to full commit info to be able to persist it
  // TODO: remove that and change Slick persistence to take PartialCommitInfo
  def toCommitInfo = CommitInfo(sha, message, authorName, authorEmail, authorName, authorEmail, date, date, List.empty[String])

}

object PartialCommitInfo {

  def apply(commitInfo: CommitInfo) = {
    new PartialCommitInfo(commitInfo.id, commitInfo.sha, commitInfo.message, commitInfo.authorName, commitInfo.authorEmail, commitInfo.commitDate)
  }

  implicit object CommitLikePartialCommitInfo extends CommitLike[PartialCommitInfo] {
    def authorName(commitLike: PartialCommitInfo) = commitLike.authorName
    def authorEmail(commitLike: PartialCommitInfo) = commitLike.authorEmail
  }

}