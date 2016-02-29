package com.softwaremill.codebrag.finders.team

import org.bson.types.ObjectId

case class ManagedTeamMemberView(teamId: ObjectId, userId: ObjectId, email: String, member: Boolean, contributor: Boolean)
