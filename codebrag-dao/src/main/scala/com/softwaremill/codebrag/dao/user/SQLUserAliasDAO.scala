package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.UserAlias

class SQLUserAliasDAO(val database: SQLDatabase) extends UserAliasDAO with SQLUserSchema {

  import database.driver.simple._
  import database._

  override def remove(aliasId: ObjectId) = db.withSession {
    implicit session =>
      userAliases.where(_.id === aliasId).delete
  }

  override def save(alias: UserAlias) = db.withSession {
    implicit session =>
      userAliases += toSQLUserAlias(alias)
  }

  override def findAllForUser(userId: ObjectId) = db.withSession {
    implicit session =>
      userAliases.where(_.userId === userId).list().map(_.toUserAlias)
  }

  override def findByAlias(alias: String) = db.withSession {
    implicit session =>
      userAliases.where(_.alias === alias).firstOption.map(_.toUserAlias)

  }

  override def findById(aliasId: ObjectId) = db.withSession {
    implicit session =>
      userAliases.where(_.id === aliasId).firstOption.map(_.toUserAlias)
  }
}
