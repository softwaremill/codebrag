package com.softwaremill.codebrag.dao.reporting.views

import java.util.Date

case class FollowupListView(followups: List[SingleFollowupView])

case class SingleFollowupView(userId: String, date: Date, commit: FollowupCommitView)

case class FollowupCommitView(commitId: String, authorName: String, message: String, date: Date)
