package com.softwaremill.codebrag.dao.invitation

import com.softwaremill.codebrag.dao.sql.{WithSQLSchemas, SQLDatabase}
import com.softwaremill.codebrag.domain.Invitation
import scala.slick.driver.JdbcProfile
import org.joda.time.DateTime
import org.bson.types.ObjectId

class SQLInvitationDAO(database: SQLDatabase) extends InvitationDAO with WithSQLSchemas {
  import database.driver.simple._
  import database._

  def save(invitation: Invitation) = db.withTransaction { implicit session =>
    invitations += invitation
  }

  def findByCode(code: String): Option[Invitation] = db.withTransaction { implicit session =>
    invitations.filter(_.code === code).firstOption
  }

  def removeByCode(code: String) {
    db.withTransaction { implicit session =>
      invitations.filter(_.code === code).delete
    }
  }

  private class Invitations(tag: Tag) extends Table[Invitation](tag, "invitations") {
    def code = column[String]("code", O.PrimaryKey)
    def invitationSender = column[ObjectId]("invitation_sender")
    def expiryDate = column[DateTime]("expiryDate")

    def * = (code, invitationSender, expiryDate) <> (Invitation.tupled, Invitation.unapply)
  }

  private val invitations = TableQuery[Invitations]

  def schemas: Iterable[JdbcProfile#DDLInvoker] = List(invitations.ddl)
}
