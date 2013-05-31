package com.softwaremill.codebrag.dao.reporting.views

import java.util.Date

case class CommitListView(commits: List[CommitView], totalCount: Int)

case class CommitView(id: String, sha: String, message: String, authorName: String, committerName: String, date: Date, pendingReview: Boolean = true)
