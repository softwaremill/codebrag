package com.softwaremill.codebrag.usecases.team

import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.scalaval.Validation._
import com.softwaremill.codebrag.dao.user.TeamDAO
import com.softwaremill.codebrag.domain.Team

class AddTeamUseCase(val teamDao: TeamDAO) extends Logging {

  def execute(teamId: ObjectId, name: String): Either[Errors, Team] = {
    val newTeam = Team(teamId, name, List())
    validateNewTeam(newTeam).whenOk[Team] {
      teamDao.add(newTeam)
      newTeam
    }
  }

  private def validateNewTeam(team: Team) = {
    val existingTeamOpt = teamDao.findByName(team.name)
    val existsCheck = rule("team")(!existingTeamOpt.exists(_.name == team.name), "This team already exists")
    validate(existsCheck)
  }
}
