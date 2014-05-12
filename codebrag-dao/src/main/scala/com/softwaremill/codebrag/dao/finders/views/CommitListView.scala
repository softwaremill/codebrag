package com.softwaremill.codebrag.dao.finders.views

import java.util.Date
import com.softwaremill.codebrag.domain.{User, CommitLike}
import com.softwaremill.codebrag.dao.finders.views.CommitState.CommitReviewState

case class CommitListView(commits: List[CommitView], older: Int, newer: Int)

case class CommitView(id: String, sha: String, message: String, authorName: String, authorEmail: String,
                      date: Date, state: CommitReviewState = CommitState.AwaitingOthersReview, authorAvatarUrl: String = "", reviewers: Set[CommitReviewerView] = Set.empty)

object CommitState extends Enumeration {
  type CommitReviewState = Value
  val AwaitingUserReview, ReviewedByUser, AwaitingOthersReview, ReviewedByOthers, NotApplicable = Value
}

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
