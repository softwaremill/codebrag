package com.softwaremill.codebrag.dao.reporting

import java.util.Date

case class FollowupsList(followups: List[SingleFollowupInfo])

case class SingleFollowupInfo(userId: String, date: Date, commit: FollowupCommitInfo)

case class FollowupCommitInfo(commitId: String, authorName: String, message: String, date: Date)
