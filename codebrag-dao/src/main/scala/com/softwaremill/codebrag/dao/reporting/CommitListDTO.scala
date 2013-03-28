package com.softwaremill.codebrag.dao.reporting

import java.util.Date

case class CommitListDTO(commits: List[CommitListItemDTO])

case class CommitListItemDTO(id: String, sha: String, message: String, authorName: String, committerName: String, date: Date)
