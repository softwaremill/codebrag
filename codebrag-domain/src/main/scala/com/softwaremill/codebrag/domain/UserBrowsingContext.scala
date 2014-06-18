package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId

case class UserBrowsingContext(userId: ObjectId, repoName: String, branchName: String, default: Boolean = false)
