package com.softwaremill.codebrag.usecases.team

import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.scalaval.Validation._
import com.softwaremill.codebrag.dao.user.TeamDAO
import com.softwaremill.codebrag.domain.Team
import com.softwaremill.codebrag.domain.TeamMember

class AddTeamMemberUseCase(val teamDao: TeamDAO) extends Logging {

  def execute(teamId: ObjectId, userId: ObjectId): Either[Errors, Team] = {
    validateTeam(teamId, userId).whenOk {
      val team = teamDao.findById(teamId).get
      val teamMembers = team.teamMembers
      val newTeamMembers = teamMembers.::(new TeamMember(teamId, userId))
      val newTeam = team.copy(teamMembers = newTeamMembers)
      teamDao.modifyTeam(newTeam)
      newTeam
    }
  }

  private def validateTeam(teamId: ObjectId, userId: ObjectId) = {
    val existingTeamOpt = teamDao.findById(teamId)
    val existingTeamMemberOpt = existingTeamOpt.get.teamMembers.find(_.user_id == userId)
    val existsCheck = rule("user")(existingTeamMemberOpt == None, "This user already is a member of this team")
    validate(existsCheck)
  }
}
