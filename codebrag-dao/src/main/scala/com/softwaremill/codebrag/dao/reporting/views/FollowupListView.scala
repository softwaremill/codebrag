package com.softwaremill.codebrag.dao.reporting.views

import java.util.Date

case class SingleFollowupView(followupId: String, date: Date, commit: FollowupCommitView, reaction: FollowupLastReactionView)

case class FollowupCommitView(commitId: String, authorName: String, message: String, date: Date)

case class FollowupsByCommitListView(followupsByCommit: List[FollowupsByCommitView])

case class FollowupsByCommitView(commit: FollowupCommitView, followups: List[FollowupReactionsView])

case class FollowupReactionsView(followupId: String, lastReaction: FollowupLastReactionView, allReactions: List[String])

case class FollowupLastReactionView(reactionId: String, reactionAuthor: String, date: Date, reactionAuthorAvatarUrl: Option[String])
