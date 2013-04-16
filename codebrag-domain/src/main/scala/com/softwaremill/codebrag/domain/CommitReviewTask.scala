package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId

case class CommitReviewTask(val commitId: ObjectId, val userId: ObjectId)
