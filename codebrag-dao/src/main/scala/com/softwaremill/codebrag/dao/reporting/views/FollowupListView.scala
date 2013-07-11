package com.softwaremill.codebrag.dao.reporting.views

import java.util.Date

case class FollowupListView(followups: List[FollowupView])

case class FollowupView(followupId: String, date: Date, commit: FollowupCommitView, reaction: FollowupReactionView)

case class FollowupCommitView(commitId: String, authorName: String, message: String, date: Date)

case class FollowupReactionView(reactionId: String, reactionAuthor: String, reactionAuthorAvatarUrl: Option[String])


case class FollowupsByCommitListView(followupsByCommit: List[FollowupsByCommitView])

case class FollowupsByCommitView(commit: FollowupCommitView, followups: List[FollowupReactions])

case class FollowupReactions(followupId: String, lastReaction: FollowupLastReactionView, allReactions: List[String])

case class FollowupLastReactionView(reactionId: String, reactionAuthor: String, date: Date, reactionAuthorAvatarUrl: Option[String])
