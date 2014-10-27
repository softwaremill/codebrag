package com.softwaremill.codebrag.dao.finders.views

import org.joda.time.DateTime

case class SingleFollowupView(followupId: String, date: DateTime, commit: FollowupCommitView, reaction: FollowupLastReactionView)

case class FollowupCommitView(commitId: String, sha: String, repoName: String, authorName: String, message: String, date: DateTime)

case class FollowupsByCommitListView(followupsByCommit: List[FollowupsByCommitView])

case class FollowupsByCommitView(commit: FollowupCommitView, followups: List[FollowupReactionsView])

case class FollowupReactionsView(followupId: String, lastReaction: FollowupLastReactionView, allReactions: List[String])


trait FollowupLastReactionView {

  def reactionId: String
  def reactionAuthor: String
  def date: DateTime
  def reactionAuthorAvatarUrl: String

}

case class FollowupLastCommentView(reactionId: String, reactionAuthor: String, date: DateTime, reactionAuthorAvatarUrl: String, message: String) extends FollowupLastReactionView

case class FollowupLastLikeView(reactionId: String, reactionAuthor: String, date: DateTime, reactionAuthorAvatarUrl: String) extends FollowupLastReactionView

