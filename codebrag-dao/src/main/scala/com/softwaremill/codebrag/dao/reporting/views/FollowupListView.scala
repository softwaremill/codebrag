package com.softwaremill.codebrag.dao.reporting.views

import java.util.Date

case class FollowupListView(followups: List[FollowupView])

case class FollowupView(followupId: String, userId: String, date: Date, commit: FollowupCommitView, reaction: FollowupReactionView)

case class FollowupCommitView(commitId: String, authorName: String, message: String, date: Date)

case class FollowupReactionView(reactionId: String, reactionAuthor: String)
