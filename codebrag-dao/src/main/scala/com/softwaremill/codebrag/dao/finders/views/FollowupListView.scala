package com.softwaremill.codebrag.dao.finders.views

import java.util.Date

case class SingleFollowupView(followupId: String, date: Date, commit: FollowupCommitView, reaction: FollowupLastReactionView)

case class FollowupCommitView(commitId: String, sha: String, repoName: String, authorName: String, message: String, date: Date)

case class FollowupsByCommitListView(followupsByCommit: List[FollowupsByCommitView])

case class FollowupsByCommitView(commit: FollowupCommitView, followups: List[FollowupReactionsView])

case class FollowupReactionsView(followupId: String, lastReaction: FollowupLastReactionView, allReactions: List[String])


trait FollowupLastReactionView {

  def reactionId: String
  def reactionAuthor: String
  def date: Date
  def reactionAuthorAvatarUrl: String

}

case class FollowupLastCommentView(reactionId: String, reactionAuthor: String, date: Date, reactionAuthorAvatarUrl: String, message: String) extends FollowupLastReactionView

case class FollowupLastLikeView(reactionId: String, reactionAuthor: String, date: Date, reactionAuthorAvatarUrl: String) extends FollowupLastReactionView

