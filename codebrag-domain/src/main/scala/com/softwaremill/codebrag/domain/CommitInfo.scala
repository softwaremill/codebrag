package com.softwaremill.codebrag.domain

import org.joda.time.DateTime
import org.bson.types.ObjectId

case class CommitInfo(id: ObjectId, sha: String, message: String, authorName: String, committerName: String, date: DateTime, parents: List[String], files: List[CommitFileInfo])

case class CommitFileInfo(filename: String, patch: String)