package com.softwaremill.codebrag.domain

import org.joda.time.DateTime
import org.bson.types.ObjectId

case class CommitInfo(id: ObjectId, sha: String, message: String, authorName: String, committerName: String, authorDate: DateTime,
                      commitDate: DateTime, parents: List[String], files: List[CommitFileInfo])

object CommitInfo {
  def apply(sha: String, message: String, authorName: String, committerName: String, authorDate: DateTime,
            commitDate: DateTime, parents: List[String], files: List[CommitFileInfo]) = {
    new CommitInfo(new ObjectId(), sha, message, authorName, committerName, authorDate, commitDate, parents, files)
  }

  implicit object CommitLikeCommitInfo extends CommitLike[CommitInfo] {
    override def authorName(commitLike: CommitInfo): String = commitLike.authorName
  }
}

case class CommitFileInfo(filename: String, status: String, patch: String)

trait CommitLike[T] {
  def authorName(commitLike: T): String
}
