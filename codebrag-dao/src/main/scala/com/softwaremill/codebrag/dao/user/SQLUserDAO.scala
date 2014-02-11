package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.domain._
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
      lastNotifs += toSQLLastNotif(user.id, user.notifications)
      userSettings += toSQLSettings(user.id, user.settings)
      auths += toSQLAuth(user.id, user.authentication)
      users += tuple(user)
    }

    user
  }

  def findAll() = db.withTransaction { implicit session =>
    val q = for {
      u <- users
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
      u <- users
      a <- u.auth if a.usernameLowerCase === login.toLowerCase
      s <- u.settings
      l <- u.lastNotif
    } yield (u, a, s, l)

    q.firstOption.map(untuple)
  }

  def findByLoginOrEmail(login: String, email: String) = db.withTransaction { implicit session =>
    val q = for {
      u <- users
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

  private case class SQLAuth(id: ObjectId, provider: String, username: String, usernameLowerCase: String, token: String, salt: String) {
    def toAuth = Authentication(provider, username, usernameLowerCase, token, salt)
  }
  private def toSQLAuth(id: ObjectId, auth: Authentication) = SQLAuth(id, auth.provider, auth.username, auth.usernameLowerCase,
    auth.token, auth.salt)

  private case class SQLSettings(id: ObjectId, avatarUrl: String, emailNotificationsEnabled: Boolean,
    dailyUpdatesEmailEnabled: Boolean, appTourDone: Boolean) {
    def toSettings = UserSettings(avatarUrl, emailNotificationsEnabled, dailyUpdatesEmailEnabled, appTourDone)
  }
  private def toSQLSettings(id: ObjectId, settings: UserSettings) = SQLSettings(id, settings.avatarUrl,
    settings.emailNotificationsEnabled, settings.dailyUpdatesEmailEnabled, settings.appTourDone)

  private case class SQLLastNotif(id: ObjectId, commits: Option[DateTime], followups: Option[DateTime]) {
    def toLastNotif = LastUserNotificationDispatch(commits, followups)
  }
  private def toSQLLastNotif(id: ObjectId, lastNotif: LastUserNotificationDispatch) = SQLLastNotif(id,
    lastNotif.commits, lastNotif.followups)

  private type UserTuple = (ObjectId, String, String, String)

  private class Auths(tag: Tag) extends Table[SQLAuth](tag, "users_authentications") {
    def userId = column[ObjectId]("user_id", O.PrimaryKey)
    def provider = column[String]("provider")
    def username = column[String]("username")
    def usernameLowerCase = column[String]("username_lowercase")
    def token = column[String]("token")
    def salt = column[String]("salt")

    def * = (userId, provider, username, usernameLowerCase, token, salt) <> (SQLAuth.tupled, SQLAuth.unapply)
  }

  private val auths = TableQuery[Auths]
  
  private class Settings(tag: Tag) extends Table[SQLSettings](tag, "users_settings") {
    def userId = column[ObjectId]("user_id", O.PrimaryKey)
    def avatarUrl = column[String]("avatar_url")
    def emailNotificationsEnabled = column[Boolean]("email_notif")
    def dailyUpdatesEmailEnabled = column[Boolean]("email_daily_updates")
    def appTourDone = column[Boolean]("app_tour_done")

    def * = (userId, avatarUrl, emailNotificationsEnabled, dailyUpdatesEmailEnabled, appTourDone) <> (SQLSettings.tupled, SQLSettings.unapply)
  }

  private val userSettings = TableQuery[Settings]

  private class LastNotifs(tag: Tag) extends Table[SQLLastNotif](tag, "users_last_notifs") {
    def userId = column[ObjectId]("user_id", O.PrimaryKey)
    def lastCommitsDispatch = column[Option[DateTime]]("last_commits_dispatch")
    def lastFollowupsDispatch = column[Option[DateTime]]("last_followups_dispatch")

    def * = (userId, lastCommitsDispatch, lastFollowupsDispatch) <> (SQLLastNotif.tupled, SQLLastNotif.unapply)
  }

  private val lastNotifs = TableQuery[LastNotifs]

  private class Users(tag: Tag) extends Table[UserTuple](tag, "users") {
    def id = column[ObjectId]("id", O.PrimaryKey)
    def name = column[String]("name")
    def emailLowerCase = column[String]("email_lowercase")
    def token = column[String]("token")

    def auth = foreignKey("AUTH_FK", id, auths)(_.userId, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
    def settings = foreignKey("SETTINGS_FK", id, userSettings)(_.userId, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
    def lastNotif = foreignKey("LAST_NOTIFS_FK", id, lastNotifs)(_.userId, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)

    def * = (id, name, emailLowerCase, token)
  }

  private val users = TableQuery[Users]

  private def tuple(user: User): UserTuple = (user.id, user.name, user.emailLowerCase, user.token)

  private val untuple: ((UserTuple, SQLAuth, SQLSettings, SQLLastNotif)) => User = {
    case (tuple, sqlAuth, sqlSettings, sqlLastNotif) =>
      User(tuple._1, sqlAuth.toAuth, tuple._2, tuple._3, tuple._4, sqlSettings.toSettings, sqlLastNotif.toLastNotif)
  }

  private def findOneWhere(condition: Users => Column[Boolean]): Option[User] = db.withTransaction { implicit session =>
    val q = for {
      u <- users if condition(u)
      a <- u.auth
      s <- u.settings
      l <- u.lastNotif
    } yield (u, a, s, l)

    q.firstOption.map(untuple)
  }

  def schemas: List[JdbcProfile#DDLInvoker] = List(lastNotifs.ddl, userSettings.ddl, auths.ddl, users.ddl)
}