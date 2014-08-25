package com.softwaremill.codebrag.domain

object CommitAuthorClassification {
  def commitAuthoredByUser[T, S](commit: T, user: S)(implicit commitLike: CommitLike[T], userLike: UserLike[S]): Boolean = {
    val nameMatches = commitLike.authorName(commit).toLowerCase == userLike.userFullName(user).toLowerCase
    val emailMatches = userLike.userEmails(user).map(_.toLowerCase).contains(commitLike.authorEmail(commit).toLowerCase)
    nameMatches || emailMatches
  }
}
