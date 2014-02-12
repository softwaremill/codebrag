package com.softwaremill.codebrag.dao.commitinfo

import org.bson.types.ObjectId
import java.util.Date

case class PartialCommitInfo(id: ObjectId, sha: String, message: String, authorName: String, authorEmail: String, date: Date)
