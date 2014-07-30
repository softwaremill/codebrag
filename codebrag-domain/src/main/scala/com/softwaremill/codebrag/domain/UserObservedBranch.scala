package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId

case class UserObservedBranch(id: ObjectId, userId: ObjectId, repoName: String, branchName: String)
