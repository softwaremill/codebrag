package com.softwaremill.codebrag.domain

import org.joda.time.DateTime
import org.bson.types.ObjectId


case class CommitComment(id: ObjectId,commitId: ObjectId, authorId: ObjectId, message: String, postingTime: DateTime)