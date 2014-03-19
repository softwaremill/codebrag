package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId
import org.joda.time.DateTime

case class ReviewedCommit(sha: String, userId: ObjectId, date: DateTime)