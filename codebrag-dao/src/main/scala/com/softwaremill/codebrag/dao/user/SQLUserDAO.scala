package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.domain._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.sql.{SQLDatabase}

class SQLUserDAO(val database: SQLDatabase) extends UserDAO with SQLUserSchema {
  import database.driver.simple._
  import database._

  def addWithId(user: User) = {
    db.withTransaction { implicit session =>
      lastNotifs += toSQLLastNotif(user.id, user.notifications)
      userSettings += toSQLSettings(user.id, user.settings)
      auths += toSQLAuth(user.id, user.authentication)
      users += tuple(user)
    }

    user
  }

  def findAll() = db.withTransaction { implicit session =>
    val q = for {
      u <- users if u.regular
      a <- u.auth
      s <- u.settings
      l <- u.lastNotif
    } yield (u, a, s, l)

    q.list().map(untuple)
  }

  def findById(userId: ObjectId) = findOneWhere(_.id === userId)

  def findByEmail(email: String) = findOneWhere(_.emailLowerCase === email.toLowerCase)

  def findByLowerCasedLogin(login: String) = db.withTransaction { implicit session =>
    val q = for {
      u <- users if u.regular
      a <- u.auth if a.usernameLowerCase === login.toLowerCase
      s <- u.settings
      l <- u.lastNotif
    } yield (u, a, s, l)

    q.firstOption.map(untuple)
  }

  def findByLoginOrEmail(login: String, email: String) = db.withTransaction { implicit session =>
    val q = for {
      u <- users if u.regular
      a <- u.auth
      if a.usernameLowerCase === login.toLowerCase || u.emailLowerCase === email.toLowerCase
      s <- u.settings
      l <- u.lastNotif
    } yield (u, a, s, l)

    q.firstOption.map(untuple)
  }

  def findByToken(token: String) = findOneWhere(_.token === token)

  def findCommitAuthor(commit: CommitInfo) = findOneWhere { u =>
    u.name === commit.authorName || u.emailLowerCase === commit.authorEmail.toLowerCase
  }

  def changeAuthentication(id: ObjectId, auth: Authentication) = db.withTransaction { implicit session =>
    auths.where(_.userId === id).update(toSQLAuth(id, auth))
  }

  def rememberNotifications(id: ObjectId, notifications: LastUserNotificationDispatch) = db.withTransaction { implicit session =>
    lastNotifs.where(_.userId === id).update(toSQLLastNotif(id, notifications))
  }

  def changeUserSettings(id: ObjectId, newSettings: UserSettings) = db.withTransaction { implicit session =>
    userSettings.where(_.userId === id).update(toSQLSettings(id, newSettings))
  }

  def findPartialUserDetails(names: Iterable[String], emails: Iterable[String]) =
    findPartialUserDetails(u => (u.name inSet names.toSet) || (u.emailLowerCase inSet emails.toSet))

  def findPartialUserDetails(ids: Iterable[ObjectId]) = findPartialUserDetails(_.id inSet ids.toSet)

  private def findPartialUserDetails(condition: Users => Column[Boolean]) = db.withTransaction { implicit session =>
    val q = for {
      u <- users if condition(u)
      s <- u.settings
    } yield (u.id, u.name, u.emailLowerCase, s.avatarUrl)

    q.list().map(PartialUserDetails.tupled)
  }

  private def findOneWhere(condition: Users => Column[Boolean]): Option[User] = db.withTransaction { implicit session =>
    val q = for {
      u <- users if condition(u) && u.regular
      a <- u.auth
      s <- u.settings
      l <- u.lastNotif
    } yield (u, a, s, l)

    q.firstOption.map(untuple)
  }
}