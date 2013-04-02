package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId

case class FollowUp(commitId: ObjectId, user_id: ObjectId, status: FollowUpStatus.Value)

object FollowUpStatus extends Enumeration {

  val New = Value("NEW")
  val Seen = Value("SEEN")
  val Ignored = Value("IGNORED")

}
