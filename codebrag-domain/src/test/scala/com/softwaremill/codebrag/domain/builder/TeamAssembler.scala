package com.softwaremill.codebrag.domain.builder

import java.util.UUID

import com.softwaremill.codebrag.domain._
import org.bson.types.ObjectId
import org.joda.time.{ DateTime, DateTimeZone }

class TeamAssembler(var team: Team) {

  def withName(name: String) = {
    team = team.copy(name = name)
    this
  }

  def withId(id: ObjectId) = {
    team = team.copy(id = id)
    this
  }

  def withMembers(members: List[TeamMember]) = {
    team = team.copy(teamMembers = members)
    this
  }

  def get = team
}

object TeamAssembler {
  def randomTeam(user: User) = new TeamAssembler(createRandomTeam(user))
  private def createRandomTeam(user: User) = Team(
    new ObjectId,
    user.name + "' Team",
    List(new TeamMember(new ObjectId, user.id, true)))
}
