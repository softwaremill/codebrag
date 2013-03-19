package com.softwaremill.codebrag.domain

import org.joda.time.DateTime

case class CommitInfo(sha: String, message: String, authorName: String, committerName: String, date: DateTime, parents: List[String])
