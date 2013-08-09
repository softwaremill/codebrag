package com.softwaremill.codebrag.common

import org.bson.types.ObjectId

case class LoadMoreCriteria(lastCommitId: Option[ObjectId], limit: Int)

case class LoadSurroundingsCriteria(commitId: ObjectId, loadLimit: Int)