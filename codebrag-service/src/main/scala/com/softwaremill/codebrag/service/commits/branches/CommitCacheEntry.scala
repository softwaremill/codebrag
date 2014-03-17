package com.softwaremill.codebrag.service.commits.branches

import com.softwaremill.codebrag.domain.CommitLike
import org.joda.time.DateTime

case class CommitCacheEntry(sha: String, authorName: String, authorEmail: String, commitDate: DateTime)

object CommitCacheEntry {

  implicit object CommitLikeCommitCacheEntry extends CommitLike[CommitCacheEntry] {
    def authorName(commitLike: CommitCacheEntry) = commitLike.authorName
    def authorEmail(commitLike: CommitCacheEntry) = commitLike.authorEmail
  }

}
