package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId
import org.joda.time.DateTime


case class UserRepoDetails(userId: ObjectId, repoName: String, branchName: String, toReviewSince: DateTime, default: Boolean = false)
