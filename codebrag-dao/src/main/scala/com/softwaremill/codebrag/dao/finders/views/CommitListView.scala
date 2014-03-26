package com.softwaremill.codebrag.dao.finders.views

import java.util.Date
import com.softwaremill.codebrag.domain.{User, CommitLike}

case class CommitListView(commits: List[CommitView], older: Int, newer: Int)

case class CommitView(id: String, sha: String, message: String, authorName: String, authorEmail: String,
                      date: Date, pendingReview: Boolean = true, authorAvatarUrl: String = "", reviewers: Option[Set[CommitReviewerView]] = None)

case class CommitReviewerView(id: String, fullName: String)

object CommitReviewerView {
  def apply(user: User) = new CommitReviewerView(user.id.toString, user.name)
}

object CommitView {

  implicit object CommitLikeCommitView extends CommitLike[CommitView] {
    def authorName(commitLike: CommitView) = commitLike.authorName

    def authorEmail(commitLike: CommitView) = commitLike.authorEmail
  }

}
