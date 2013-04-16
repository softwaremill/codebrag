package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId

case class CommitToReview(val commitId: ObjectId, val userId: ObjectId)
