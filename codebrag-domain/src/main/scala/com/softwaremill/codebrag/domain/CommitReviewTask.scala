package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId

case class CommitReviewTask(commitId: ObjectId, userId: ObjectId)
