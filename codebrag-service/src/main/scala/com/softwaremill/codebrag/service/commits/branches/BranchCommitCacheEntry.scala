package com.softwaremill.codebrag.service.commits.branches

import com.softwaremill.codebrag.domain.CommitLike
import org.joda.time.DateTime

case class BranchCommitCacheEntry(sha: String, authorName: String, authorEmail: String, commitDate: DateTime)

object BranchCommitCacheEntry {

  implicit object CommitLikeCommitCacheEntry extends CommitLike[BranchCommitCacheEntry] {
    def authorName(commitLike: BranchCommitCacheEntry) = commitLike.authorName
    def authorEmail(commitLike: BranchCommitCacheEntry) = commitLike.authorEmail
  }

}
