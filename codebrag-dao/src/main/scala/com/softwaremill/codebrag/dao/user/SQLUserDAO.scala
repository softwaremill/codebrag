package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.domain._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.sql.SQLDatabase
import org.joda.time.{DateTimeZone, DateTime}

class SQLUserDAO(val database: SQLDatabase) extends UserDAO with SQLUserSchema {
  import database.driver.simple._
  import database._

  def add(user: User) {
    db.withTransaction { implicit session =>
      lastNotifs += toSQLLastNotif(user.id, user.notifications)
      userSettings += toSQLSettings(user.id, user.settings)
      auths += toSQLAuth(user.id, user.authentication)
      userAliases ++= user.aliases.emailAliases.map(toSQLUserAlias)
      users += tuple(user)
    }
  }

  def findAll() = db.withTransaction { implicit session =>
    val q = for {
      u <- users if u.regular
      a <- u.auth
      s <- u.settings
      l <- u.lastNotif
    } yield (u, a, s, l)

    q.list().map(queryUserAliases).map(untuple)
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

    q.firstOption.map(queryUserAliases).map(untuple)
  }

  def findByLoginOrEmail(login: String, email: String) = db.withTransaction { implicit session =>
    val q = for {
      u <- users if u.regular
      a <- u.auth
      if a.usernameLowerCase === login.toLowerCase || u.emailLowerCase === email.toLowerCase
      s <- u.settings
      l <- u.lastNotif
    } yield (u, a, s, l)

    q.firstOption.map(queryUserAliases).map(untuple)
  }

  def findByToken(token: String) = findOneWhere(_.token === token)

  def findCommitAuthor(commit: CommitInfo) = db.withSession { implicit session =>
    findAll().find { u =>
      u.name == commit.authorName || u.emailLowerCase == commit.authorEmail.toLowerCase || u.aliases.emailAliases.map(_.alias).toSet.contains(commit.authorEmail.toLowerCase)
    }
  }

  def modifyUser(user: User) = db.withTransaction { implicit session =>
    users.where(_.id === user.id).update(tuple(user))
    auths.where(_.userId === user.id).update(toSQLAuth(user.id, user.authentication))
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

  def setToReviewStartDate(id: ObjectId, newToReviewDate: DateTime) {
    val toReviewDateAsUTC = newToReviewDate.withZone(DateTimeZone.UTC)
    db.withTransaction { implicit session =>
      userSettings.filter(_.userId === id).map(_.toReviewStartDate).update(Some(toReviewDateAsUTC))
    }
  }

  def findPartialUserDetails(names: Iterable[String], emails: Iterable[String]) =
    findPartialUserDetails(u => (u.name inSet names.toSet) || (u.emailLowerCase inSet emails.toSet))

  def findPartialUserDetails(ids: Iterable[ObjectId]) = findPartialUserDetails(_.id inSet ids.toSet)

  def countAll() = {
    db.withTransaction { implicit session =>
      Query(users.filter(_.regular is true).length).first().toLong
    }
  }

  def countAllActive() = {
    db.withTransaction { implicit session =>
      Query(users.filter(_.regular is true).filter(_.active is true).length).first().toLong
    }
  }

  private def findPartialUserDetails(condition: Users => Column[Boolean]) = db.withTransaction { implicit session =>
    val q = for {
      u <- users if condition(u)
      s <- u.settings
    } yield (u.id, u.name, u.emailLowerCase, s.avatarUrl)

    q.list().map(queryPartialUserAliases).map(PartialUserDetails.tupled)
  }

  private def findOneWhere(condition: Users => Column[Boolean]): Option[User] = db.withTransaction { implicit session =>
    val userQuery = for {
      u <- users if condition(u) && u.regular
      a <- u.auth
      s <- u.settings
      l <- u.lastNotif
    } yield (u, a, s, l)
    userQuery.firstOption.map(queryUserAliases).map(untuple)
  }
  
  private def queryUserAliases(tuple: (UserTuple, SQLAuth, SQLSettings, SQLLastNotif))(implicit session: Session) = {
    val userId = tuple._1._1
    val aliases = userAliases.where(_.userId === userId).list()
    (tuple._1, tuple._2, tuple._3, tuple._4, aliases)
  }

  private def queryPartialUserAliases(tuple: (ObjectId, String, String, String))(implicit session: Session) = {
    val aliases = userAliases.where(_.userId === tuple._1).list()
    (tuple._1, tuple._2, tuple._3, tuple._4, UserAliases(aliases.map(_.toUserAlias)))
  }

  def delete(userId: ObjectId) {
    db.withTransaction { implicit session =>
      users.filter(_.id === userId).delete
    }
  }
  
}
