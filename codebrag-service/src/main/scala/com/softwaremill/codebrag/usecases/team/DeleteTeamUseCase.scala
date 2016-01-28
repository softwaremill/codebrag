package com.softwaremill.codebrag.usecases.team

import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.scalaval.Validation._
import com.softwaremill.codebrag.dao.user.TeamDAO
import com.softwaremill.codebrag.domain.Team

class DeleteTeamUseCase(val teamDao: TeamDAO) extends Logging {

  def execute(teamId: ObjectId): Either[Errors, Unit] = {
    validateTeam(teamId).whenOk {
      teamDao.delete(teamId);
    }
  }

  private def validateTeam(teamId: ObjectId) = {
    val existingTeamOpt = teamDao.findById(teamId)
    val existsCheck = rule("team")(existingTeamOpt.exists(_.id == teamId), "This team does not exists")
    validate(existsCheck)
  }
}
