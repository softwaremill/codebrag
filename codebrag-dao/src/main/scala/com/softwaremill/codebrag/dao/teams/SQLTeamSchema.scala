package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.domain._
import org.bson.types.ObjectId
import org.joda.time.DateTime
import scala.slick.model.ForeignKeyAction

trait SQLTeamSchema {
  val database: SQLDatabase

  import database._
  import database.driver.simple._

  protected case class SQLTeamMember(team_id: ObjectId, user_id: ObjectId, contributor: Boolean = true) {
    def toTeamMember = TeamMember(team_id, user_id, contributor)
  }

  protected val teamMembers = TableQuery[TeamMembers]

  protected class Teams(tag: Tag) extends Table[TeamTuple](tag, "teams") {
    def id = column[ObjectId]("id", O.PrimaryKey)
    def name = column[String]("name")

    def teammembers = foreignKey("team_members_fk", id, teamMembers)(_.teamId, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)

    def * = (id, name)
  }

  protected class TeamMembers(tag: Tag) extends Table[SQLTeamMember](tag, "team_members") {
    def teamId = column[ObjectId]("team_id")
    def userId = column[ObjectId]("user_id")
    def contributor = column[Boolean]("contributor")

    def * = (teamId, userId, contributor) <> (SQLTeamMember.tupled, SQLTeamMember.unapply)
  }

  protected val teams = TableQuery[Teams]

  protected type TeamTuple = (ObjectId, String)

  protected def tuple(team: Team): TeamTuple = (team.id, team.name)

  protected val untuple: ((TeamTuple, List[SQLTeamMember])) => Team = {
    case (tuple, sqlTeamMembers) =>
      Team(
        tuple._1,
        tuple._2,
        sqlTeamMembers.map(_.toTeamMember).toList)
  }

}
