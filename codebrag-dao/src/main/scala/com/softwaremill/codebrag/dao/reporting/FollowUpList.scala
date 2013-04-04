package com.softwaremill.codebrag.dao.reporting

import java.util.Date

case class FollowUpsList(followUps: List[SingleFollowUpInfo])

case class SingleFollowUpInfo(userId: String, date: Date, commit: FollowUpCommitInfo)

case class FollowUpCommitInfo(commitId: String, authorName: String, message: String, date: Date)
