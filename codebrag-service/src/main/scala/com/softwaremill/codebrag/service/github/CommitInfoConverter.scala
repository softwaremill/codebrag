package com.softwaremill.codebrag.service.github

import org.eclipse.egit.github.core.RepositoryCommit
import com.softwaremill.codebrag.domain.CommitInfo

trait CommitInfoConverter[T] {
  def convertToCommitInfo(commit: T): CommitInfo
}

class GitHubCommitInfoConverter extends CommitInfoConverter[RepositoryCommit] {
  def convertToCommitInfo(commit: RepositoryCommit): CommitInfo = {
    val rawCommit = commit.getCommit
    CommitInfo(commit.getSha, rawCommit.getMessage, rawCommit.getAuthor.getName, rawCommit.getCommitter.getName)
  }
}
