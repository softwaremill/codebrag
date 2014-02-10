package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.dao.sql.{SQLDatabase, WithSQLSchemas}
import com.softwaremill.codebrag.domain.InternalUser
import org.bson.types.ObjectId
import scala.slick.driver.JdbcProfile

class SQLInternalUserDAO(database: SQLDatabase) extends InternalUserDAO with WithSQLSchemas {
  import database.driver.simple._
  import database._

  def findByName(internalUserName: String) = db.withTransaction { implicit session =>
    doFindByName(internalUserName)
  }
  
  def createIfNotExists(internalUser: InternalUser) = db.withTransaction { implicit session =>
    doFindByName(internalUser.name).getOrElse {
      internalUsers += internalUser
      internalUser
    }
  }

  private def doFindByName(internalUserName: String)(implicit session: Session) = {
    val q = for {
      iu <- internalUsers if iu.name === internalUserName
    } yield iu

    q.firstOption
  }

  private class InternalUsers(tag: Tag) extends Table[InternalUser](tag, "internal_users") {
    def id = column[ObjectId]("id", O.PrimaryKey)
    def name = column[String]("name")

    def * = (id, name) <> (InternalUser.tupled, InternalUser.unapply)
  }

  private val internalUsers = TableQuery[InternalUsers]

  def count() = db.withTransaction { implicit session => Query(internalUsers.length).first().toLong }

  def schemas: List[JdbcProfile#DDLInvoker] = List(internalUsers.ddl)
}
