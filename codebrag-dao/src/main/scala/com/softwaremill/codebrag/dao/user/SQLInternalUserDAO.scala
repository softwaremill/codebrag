package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.domain.InternalUser

class SQLInternalUserDAO(val database: SQLDatabase) extends InternalUserDAO with SQLUserSchema {
  import database.driver.simple._
  import database._

  def findByName(internalUserName: String) = db.withTransaction { implicit session =>
    doFindByName(internalUserName)
  }
  
  def createIfNotExists(internalUser: InternalUser) = db.withTransaction { implicit session =>
    doFindByName(internalUser.name).getOrElse {
      val id = internalUser.id

      // we have a schema, and we have to stick to it ...
      lastNotifs += SQLLastNotif(id, None, None)
      userSettings += SQLSettings(id, "", emailNotificationsEnabled = false, dailyUpdatesEmailEnabled = false, appTourDone = false, toReviewStartDate = None)
      auths += SQLAuth(id, "", "", "", "", "")
      users += (id, internalUser.name, "", "", false)

      internalUser
    }
  }

  private def doFindByName(internalUserName: String)(implicit session: Session) = {
    val q = for {
      u <- users if u.name === internalUserName && u.regular === false
    } yield u

    q.firstOption.map(u => InternalUser(u._1, u._2))
  }

  def count() = db.withTransaction { implicit session => Query(users.length).first().toLong }
}
