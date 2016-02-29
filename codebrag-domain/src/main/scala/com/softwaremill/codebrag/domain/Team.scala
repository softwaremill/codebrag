package com.softwaremill.codebrag.domain

import com.softwaremill.codebrag.common.Utils
import org.bson.types.ObjectId

case class Team(id: ObjectId, name: String, teamMembers: List[TeamMember] = null)

case class TeamMember(team_id: ObjectId, user_id: ObjectId, contributor: Boolean = true) {

}