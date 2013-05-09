package com.softwaremill.codebrag.dao.reporting.views

import java.util.Date

case class FollowupListView(followups: List[FollowupView])

case class FollowupView(followupId: String, userId: String, date: Date, commit: FollowupCommitView, comment:  FollowupCommentView)

case class FollowupCommitView(commitId: String, authorName: String, message: String, date: Date)

case class FollowupCommentView(commentId: String, commenterName: String)
