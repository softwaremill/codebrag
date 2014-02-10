package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.dao.sql.{SQLDatabase, WithSQLSchema}
import com.softwaremill.codebrag.domain.InternalUser
import org.bson.types.ObjectId
import scala.slick.driver.JdbcProfile

class SQLInternalUserDAO(db: SQLDatabase) extends InternalUserDAO with WithSQLSchema {
  import db.driver.simple._
  import db.db._

  def findByName(internalUserName: String) = withTransaction { implicit session =>
    doFindByName(internalUserName)
  }
  
  def createIfNotExists(internalUser: InternalUser) = withTransaction { implicit session =>
    doFindByName(internalUser.name).getOrElse {
      internalUsers += (internalUser.id.toString, internalUser.name)
      internalUser
    }
  }

  private def doFindByName(internalUserName: String)(implicit session: Session) = {
    val q = for {
      iu <- internalUsers if iu.name === internalUserName
    } yield iu

    q.firstOption.map { case (id, name) => InternalUser(new ObjectId(id), name) }
  }

  private class InternalUsers(tag: Tag) extends Table[(String, String)](tag, "internal_users") {
    def id = column[String]("id", O.PrimaryKey)
    def name = column[String]("name")

    def * = (id, name)
  }

  private val internalUsers = TableQuery[InternalUsers]

  def count() = withTransaction { implicit session => Query(internalUsers.length).first().toLong }

  def schema: JdbcProfile#DDLInvoker = internalUsers.ddl
}
