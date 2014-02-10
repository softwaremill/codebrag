package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.domain._
import com.softwaremill.codebrag.domain.LastUserNotificationDispatch
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.sql.{WithSQLSchemas, SQLDatabase}
import scala.slick.driver.JdbcProfile
import org.joda.time.DateTime
import scala.slick.model.ForeignKeyAction

class SQLUserDAO(database: SQLDatabase) extends UserDAO with WithSQLSchemas {
  import database.driver.simple._
  import database._

  def addWithId(user: User) = {
    db.withTransaction { implicit session =>
      lastNotifs += tupleLastNotif(user.id, user.notifications)
      userSettings += tupleSettings(user.id, user.settings)
      auths += tupleAuth(user)
      users += tuple(user)
    }

    user
  }

  def findAll() = db.withTransaction { implicit session =>
    val q = for {
      u <- users
      a <- u.authJoin
      s <- u.settingsJoin
      l <- u.lastNotifJoin
    } yield (u, a, s, l)

    q.list().map(untuple)
  }

  def findById(userId: ObjectId) = findOneWhere(_.id === userId)

  def findByEmail(email: String) = findOneWhere(_.emailLowerCase === email.toLowerCase)

  def findByLowerCasedLogin(login: String) = db.withTransaction { implicit session =>
    val q = for {
      u <- users
      a <- u.authJoin if a.usernameLowerCase === login.toLowerCase
      s <- u.settingsJoin
      l <- u.lastNotifJoin
    } yield (u, a, s, l)

    q.firstOption.map(untuple)
  }

  def findByLoginOrEmail(login: String, email: String) = db.withTransaction { implicit session =>
    val q = for {
      u <- users
      a <- u.authJoin
      if a.usernameLowerCase === login.toLowerCase || u.emailLowerCase === email.toLowerCase
      s <- u.settingsJoin
      l <- u.lastNotifJoin
    } yield (u, a, s, l)

    q.firstOption.map(untuple)
  }

  def findByToken(token: String) = findOneWhere(_.token === token)

  def findCommitAuthor(commit: CommitInfo) = findOneWhere { u =>
    u.name === commit.authorName || u.emailLowerCase === commit.authorEmail.toLowerCase
  }

  def changeAuthentication(id: ObjectId, auth: Authentication) = db.withTransaction { implicit session =>
    auths.where(_.id === id).update(tupleAuth(id, auth))
  }

  def rememberNotifications(id: ObjectId, notifications: LastUserNotificationDispatch) = db.withTransaction { implicit session =>
    lastNotifs.where(_.id === id).update(tupleLastNotif(id, notifications))
  }

  def changeUserSettings(id: ObjectId, newSettings: UserSettings) = db.withTransaction { implicit session =>
    userSettings.where(_.id === id).update(tupleSettings(id, newSettings))
  }

  private type AuthTuple = (ObjectId, String, String, String, String, String)
  private type SettingsTuple = (ObjectId, String, Boolean, Boolean, Boolean)
  private type LastNotifTuple = (ObjectId, Option[DateTime], Option[DateTime])
  private type UserTuple = (ObjectId, String, String, String)

  private class Auths(tag: Tag) extends Table[AuthTuple](tag, "users_authentications") {
    def id = column[ObjectId]("id", O.PrimaryKey)
    def provider = column[String]("provider")
    def username = column[String]("username")
    def usernameLowerCase = column[String]("username_lowercase")
    def token = column[String]("token")
    def salt = column[String]("salt")

    def * = (id, provider, username, usernameLowerCase, token, salt)
  }

  private val auths = TableQuery[Auths]
  
  private class Settings(tag: Tag) extends Table[SettingsTuple](tag, "users_settings") {
    def id = column[ObjectId]("id", O.PrimaryKey)
    def avatarUrl = column[String]("avatar_url")
    def emailNotificationsEnabled = column[Boolean]("email_notif")
    def dailyUpdatesEmailEnabled = column[Boolean]("email_daily_updates")
    def appTourDone = column[Boolean]("app_tour_done")

    def * = (id, avatarUrl, emailNotificationsEnabled, dailyUpdatesEmailEnabled, appTourDone)
  }

  private val userSettings = TableQuery[Settings]

  private class LastNotifs(tag: Tag) extends Table[LastNotifTuple](tag, "users_last_notifs") {
    def id = column[ObjectId]("id", O.PrimaryKey)
    def lastCommitsDispatch = column[Option[DateTime]]("last_commits_dispatch")
    def lastFollowupsDispatch = column[Option[DateTime]]("last_followups_dispatch")

    def * = (id, lastCommitsDispatch, lastFollowupsDispatch)
  }

  private val lastNotifs = TableQuery[LastNotifs]

  private class Users(tag: Tag) extends Table[UserTuple](tag, "users") {
    def id = column[ObjectId]("id", O.PrimaryKey)
    def name = column[String]("name")
    def emailLowerCase = column[String]("email_lowercase")
    def token = column[String]("token")

    def auth = foreignKey("AUTH_FK", id, auths)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
    def authJoin = auths.where(_.id === id)

    def settings = foreignKey("SETTINGS_FK", id, userSettings)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
    def settingsJoin = userSettings.where(_.id === id)

    def lastNotif = foreignKey("LAST_NOTIFS_FK", id, lastNotifs)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
    def lastNotifJoin = lastNotifs.where(_.id === id)

    def * = (id, name, emailLowerCase, token)
  }

  private val users = TableQuery[Users]

  private def tuple(user: User): UserTuple = {
    (user.id,
      user.name, user.emailLowerCase, user.token)
  }

  private def tupleAuth(user: User): AuthTuple = tupleAuth(user.id, user.authentication)

  private def tupleAuth(id: ObjectId, auth: Authentication): AuthTuple = {
    (id,
      auth.provider, auth.username, auth.usernameLowerCase,
      auth.token, auth.salt)
  }

  private def tupleSettings(id: ObjectId, settings: UserSettings): SettingsTuple =
    (id,
      settings.avatarUrl, settings.emailNotificationsEnabled,
      settings.dailyUpdatesEmailEnabled, settings.appTourDone)

  private def tupleLastNotif(id: ObjectId, lastNotif: LastUserNotificationDispatch): LastNotifTuple =
    (id, lastNotif.commits, lastNotif.followups)

  private val untuple: ((UserTuple, AuthTuple, SettingsTuple, LastNotifTuple)) => User = {
    case (tuple, authTuple, settingsTuple, lastNotifTuple) =>
      User(tuple._1,
        Authentication(authTuple._2, authTuple._3, authTuple._4, authTuple._5, authTuple._6),
        tuple._2, tuple._3, tuple._4,
        UserSettings(settingsTuple._2, settingsTuple._3, settingsTuple._4, settingsTuple._5),
        LastUserNotificationDispatch(lastNotifTuple._2, lastNotifTuple._3))
  }

  private def findOneWhere(condition: Users => Column[Boolean]): Option[User] = db.withTransaction { implicit session =>
    val q = for {
      u <- users if condition(u)
      a <- u.authJoin
      s <- u.settingsJoin
      l <- u.lastNotifJoin
    } yield (u, a, s, l)

    q.firstOption.map(untuple)
  }

  def schemas: List[JdbcProfile#DDLInvoker] = List(lastNotifs.ddl, userSettings.ddl, auths.ddl, users.ddl)
}