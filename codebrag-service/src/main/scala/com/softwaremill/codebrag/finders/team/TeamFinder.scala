package com.softwaremill.codebrag.finders.team

import com.softwaremill.codebrag.dao.user.{UserDAO, TeamDAO}
import com.softwaremill.codebrag.domain.{User, Team}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.finders.browsingcontext.UserBrowsingContextFinder
import org.bson.types.ObjectId

class TeamFinder(userDao: UserDAO, teamDao: TeamDAO) extends Logging {

  def findAllTeams(): List[Team] = teamDao.findAll()

  def findTeam(teamId: ObjectId): Team = teamDao.findById(teamId).get

  def findAllAsManagedTeamMembers(team: Team): ManagedTeamMembersListView = ManagedTeamMembersListView(userDao.findAll().map(user => toManagedUser(user, team)).sortBy(_.email))

  private def toManagedUser(user: User, team: Team) = ManagedTeamMemberView(team.id, user.id, user.emailLowerCase, isMember(user, team), isContributor(user, team))

  private def isMember(user: User, team: Team): Boolean = {
    for (member <- team.teamMembers)
      if (member.user_id == user.id)
        return true;
    return false;
  }

  private def isContributor(user: User, team: Team): Boolean = {
    for (member <- team.teamMembers)
      if (member.user_id == user.id && member.contributor)
        return true;
    return false;
  }

}