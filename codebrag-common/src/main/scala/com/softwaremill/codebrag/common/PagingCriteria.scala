package com.softwaremill.codebrag.common

import org.bson.types.ObjectId

case class PagingCriteria(skip: Int, limit: Int)

case class LoadSurroundingsCriteria(commitId: ObjectId, loadLimit: Int)