package com.softwaremill.codebrag.finders.commits

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.user.{UserDAO, TeamDAO}
import com.typesafe.scalalogging.slf4j.Logging

protected[finders] trait TeamMemberLoader extends Logging {

  protected def userDao: UserDAO
  
  protected def teamDao: TeamDAO

  protected def loadTeamMembersWithDetails(userId: ObjectId) = userDao.findPartialUserDetails(loadTeamMembers(userId)).toList
  
  protected def loadTeamMembers(userId: ObjectId) = teamDao.findByUser(userId).flatMap(_.teamMembers).filter(_.contributor).map(_.user_id).distinct.toList
  
}
