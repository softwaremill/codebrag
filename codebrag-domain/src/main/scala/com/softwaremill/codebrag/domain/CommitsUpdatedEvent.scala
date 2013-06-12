package com.softwaremill.codebrag.domain

import com.softwaremill.codebrag.common.Event
import org.bson.types.ObjectId

case class CommitsUpdatedEvent(firstTime: Boolean, newCommits: List[UpdatedCommit]) extends Event

case class UpdatedCommit(id: ObjectId, authorName: String)