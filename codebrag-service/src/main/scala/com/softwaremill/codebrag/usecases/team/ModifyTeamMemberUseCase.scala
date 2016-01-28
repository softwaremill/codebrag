package com.softwaremill.codebrag.usecases.team

import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.scalaval.Validation._
import com.softwaremill.codebrag.dao.user.TeamDAO
import com.softwaremill.codebrag.domain.Team
import com.softwaremill.codebrag.domain.TeamMember

class ModifyTeamMemberUseCase(val teamDao: TeamDAO) extends Logging {

  def execute(teamId: ObjectId, userId: ObjectId, contributor: Option[Boolean]): Either[Errors, Team] = {
    validateTeam(teamId).whenOk {
      var team = teamDao.findById(teamId).get
      if (contributor != None) {
        val teamMembers = team.teamMembers
        val teamMember = teamMembers.find(_.user_id == userId).get
        val newTeamMember = teamMember.copy(contributor = contributor.get)
        team = team.copy(teamMembers = teamMembers.patch(teamMembers.indexOf(teamMember), Seq(newTeamMember), 1))
        teamDao.modifyTeam(team)
      }
      team
    }
  }

  private def validateTeam(teamId: ObjectId) = {
    val existingTeamOpt = teamDao.findById(teamId)
    val existsCheck = rule("team")(existingTeamOpt.exists(_.id == teamId), "This team does not exists")
    validate(existsCheck)
  }
}
