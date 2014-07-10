package com.softwaremill.codebrag.domain

object CommitAuthorClassification {
  def commitAuthoredByUser[T, S](commit: T, user: S)(implicit commitLike: CommitLike[T], userLike: UserLike[S]): Boolean = {
    commitLike.authorName(commit) == userLike.userFullName(user) ||
      commitLike.authorEmail(commit) == userLike.userEmails(user)
  }
}
