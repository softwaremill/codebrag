package com.softwaremill.codebrag.common

import org.bson.types.ObjectId

case class PagingCriteria(maxCommitId: Option[ObjectId], minCommitId: Option[ObjectId], limit: Int)

case class SurroundingsCriteria(commitId: ObjectId, loadLimit: Int)