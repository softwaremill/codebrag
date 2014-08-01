package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId

case class UserWatchedBranch(id: ObjectId, userId: ObjectId, repoName: String, branchName: String)
