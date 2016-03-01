package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.{ RegisterService, Authenticator }
import com.softwaremill.codebrag.service.config.CodebragConfig
import org.bson.types.ObjectId
import org.scalatra
import com.softwaremill.codebrag.usecases.team.{ AddTeamUseCase, DeleteTeamUseCase, AddTeamMemberUseCase, DeleteTeamMemberUseCase, ModifyTeamMemberUseCase }
import com.softwaremill.codebrag.finders.team.TeamFinder
import com.softwaremill.codebrag.finders.team.ManagedTeamMembersListView

class TeamsServlet(
    val authenticator: Authenticator,
    teamFinder: TeamFinder,
    addTeamUseCase: AddTeamUseCase,
    deleteTeamUseCase: DeleteTeamUseCase,
    addTeamMemberUseCase: AddTeamMemberUseCase,
    deleteTeamMemberUseCase: DeleteTeamMemberUseCase,
    modifyTeamMemberUseCase: ModifyTeamMemberUseCase,
    config: CodebragConfig) extends JsonServletWithAuthentication {

  get("/") {
    haltIfNotAuthenticated()
    teamFinder.findAllTeams()
  }

  post("/") {
    haltIfNotAuthenticated()
    val teamName = extractReq[String]("name")
    addTeamUseCase.execute(new ObjectId, teamName) match {
      case Left(errors)       => scalatra.BadRequest(errors)
      case Right(teamCreated) => scalatra.Ok(teamCreated)
    }
  }

  delete("/:teamId") {
    haltIfNotAuthenticated()
    val teamIdToRemove = new ObjectId(params("teamId"))
    deleteTeamUseCase.execute(teamIdToRemove) match {
      case Left(errors) => scalatra.BadRequest(errors)
      case _            => scalatra.Ok()
    }
  }

  get("/:teamId/members") {
    haltIfNotAuthenticated()
    if (config.demo) {
      ManagedTeamMembersListView(List.empty)
    } else {
      val team = teamFinder.findTeam(new ObjectId(params("teamId")))
      teamFinder.findAllAsManagedTeamMembers(team)
    }
  }

  post("/:teamId/members") {
    haltIfNotAuthenticated()
    val targetTeamId = new ObjectId(extractOpt[String]("teamId").get)
    val targetUserId = new ObjectId(extractOpt[String]("userId").get)
    addTeamMemberUseCase.execute(targetTeamId, targetUserId) match {
      case Left(errors) => scalatra.BadRequest(errors)
      case Right(team)  => scalatra.Ok(team)
    }
  }

  delete("/:teamId/members/:userId") {
    haltIfNotAuthenticated()
    val targetTeamId = new ObjectId(params("teamId"))
    val targetUserId = new ObjectId(params("userId"))
    deleteTeamMemberUseCase.execute(targetTeamId, targetUserId) match {
      case Left(errors) => scalatra.BadRequest(errors)
      case Right(team)  => scalatra.Ok(team)
    }
  }

  put("/:teamId/members") {
    haltIfNotAuthenticated()
    val targetTeamId = new ObjectId(extractOpt[String]("teamId").get)
    val targetUserId = new ObjectId(extractOpt[String]("userId").get)
    val contributorOpt = extractOpt[Boolean]("contributor")
    modifyTeamMemberUseCase.execute(targetTeamId, targetUserId, contributorOpt) match {
      case Left(errors) => scalatra.BadRequest(errors)
      case Right(team)  => scalatra.Ok(team)
    }
  }

  object TeamsServlet {
    val MappingPath = "teams"
  }

}
