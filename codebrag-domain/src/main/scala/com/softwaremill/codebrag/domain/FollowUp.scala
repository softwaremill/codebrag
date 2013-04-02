package com.softwaremill.codebrag.domain


case class FollowUp(commit: CommitInfo, user: User, status: FollowUpStatus.Value)

object FollowUpStatus extends Enumeration {

  val New = Value("NEW")
  val Seen = Value("SEEN")
  val Ignored = Value("IGNORED")

}
