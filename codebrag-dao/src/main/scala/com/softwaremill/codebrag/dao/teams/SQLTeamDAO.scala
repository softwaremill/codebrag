package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.domain._
import org.bson.types.ObjectId
import org.joda.time.{ DateTime, DateTimeZone }

class SQLTeamDAO(val database: SQLDatabase) extends TeamDAO with SQLTeamSchema {
  import database._
  import database.driver.simple._

  def add(team: Team) {
    db.withTransaction { implicit session =>
      teams += new TeamTuple(team.id, team.name)
    }
  }

  def countAll() = {
    db.withTransaction { implicit session =>
      Query(teams.length).first().toLong
    }
  }

  def findAll() = db.withTransaction { implicit session =>
    teams.list().map(queryTeamMembers).map(untuple)
  }

  def findById(teamId: ObjectId) = db.withTransaction { implicit session =>
    val q = for {
      t <- teams if (t.id === teamId)
    } yield (t)

    q.firstOption.map(queryTeamMembers).map(untuple)
  }

  def findByName(name: String) = db.withTransaction { implicit session =>
    val q = for {
      t <- teams if (t.name === name)
    } yield (t)

    q.firstOption.map(queryTeamMembers).map(untuple)
  }
  
  def findByUser(userId: ObjectId) = db.withTransaction { implicit session =>
    teamMembers.where(_.userId === userId).list().map(t => findById(t.team_id).get);
  }

  def modifyTeam(team: Team) = db.withTransaction { implicit session =>
    teams.where(_.id === team.id).update(tuple(team))
 
    // Remove members from database.
    teamMembers.where(_.teamId === team.id).delete
    
    // Insert new ones
    teamMembers.insertAll(team.teamMembers.map(t => SQLTeamMember(team.id, t.user_id, t.contributor)).toSeq: _*)
  }
  
  def delete(teamId: ObjectId) = db.withTransaction { implicit session =>
      teams.where(_.id === teamId).delete
      
      teamMembers.where(_.teamId === teamId).delete
  }

  private def queryTeamMembers(tuple: TeamTuple)(implicit session: Session) = {
    val teamId = tuple._1
    val members = teamMembers.where(_.teamId === teamId).list()
    (tuple, members)
  }

}