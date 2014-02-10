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
      auths += tupleAuth(user)
      users += tuple(user)
    }

    user
  }

  def findAll() = db.withTransaction { implicit session =>
    val q = for {
      u <- users
      a <- u.authJoin
    } yield (u, a)

    q.list().map(untuple)
  }

  def findById(userId: ObjectId) = findOneWhere(_.id === userId)

  def findByEmail(email: String) = findOneWhere(_.emailLowerCase === email.toLowerCase)

  def findByLowerCasedLogin(login: String) = db.withTransaction { implicit session =>
    val q = for {
      u <- users
      a <- u.authJoin if a.usernameLowerCase === login.toLowerCase
    } yield (u, a)

    q.firstOption.map(untuple)
  }

  def findByLoginOrEmail(login: String, email: String) = db.withTransaction { implicit session =>
    val q = for {
      u <- users
      a <- u.authJoin
      if a.usernameLowerCase === login.toLowerCase || u.emailLowerCase === email.toLowerCase
    } yield (u, a)

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
    val q = for {
      u <- users if u.id === id
    } yield (u.notifLastCommitsDispatch, u.notifLastFollowupsDispatch)

    q.update(notifications.commits, notifications.followups)
  }

  def changeUserSettings(id: ObjectId, newSettings: UserSettings) = db.withTransaction { implicit session =>
    val q = for {
      u <- users if u.id === id
    } yield (u.settingsAvatarUrl, u.settingsEmailNotificationsEnabled, u.settingsDailyUpdatesEmailEnabled, u.settingsAppTourDone)

    q.update(newSettings.avatarUrl, newSettings.emailNotificationsEnabled,
      newSettings.dailyUpdatesEmailEnabled, newSettings.appTourDone)
  }

  private type AuthTuple = (ObjectId, String, String, String, String, String)
  private type UserTuple = (ObjectId, String, String, String, String, Boolean, Boolean, Boolean, Option[DateTime], Option[DateTime])

  private class Auths(tag: Tag) extends Table[AuthTuple](tag, "authentications") {
    def id = column[ObjectId]("id", O.PrimaryKey)
    def provider = column[String]("provider")
    def username = column[String]("username")
    def usernameLowerCase = column[String]("username_lowercase")
    def token = column[String]("token")
    def salt = column[String]("salt")

    def * = (id, provider, username, usernameLowerCase, token, salt)
  }

  private val auths = TableQuery[Auths]

  // TODO: extract entities
  // TODO: indexes
  private class Users(tag: Tag) extends Table[UserTuple](tag, "users") {
    def id = column[ObjectId]("id", O.PrimaryKey)
    def name = column[String]("name")
    def emailLowerCase = column[String]("email_lowercase")
    def token = column[String]("token")
    def settingsAvatarUrl = column[String]("settings_avatar_url")
    def settingsEmailNotificationsEnabled = column[Boolean]("settings_email_notif")
    def settingsDailyUpdatesEmailEnabled = column[Boolean]("settings_email_daily_updates")
    def settingsAppTourDone = column[Boolean]("settings_app_tour_done")
    def notifLastCommitsDispatch = column[Option[DateTime]]("notif_last_commits_dispatch")
    def notifLastFollowupsDispatch = column[Option[DateTime]]("notif_last_followups_dispatch")

    def auth = foreignKey("AUTH_FK", id, auths)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
    def authJoin = auths.where(_.id === id)

    def * = (id, name, emailLowerCase, token,
      settingsAvatarUrl, settingsEmailNotificationsEnabled, settingsDailyUpdatesEmailEnabled, settingsAppTourDone,
      notifLastCommitsDispatch, notifLastFollowupsDispatch)
  }

  private val users = TableQuery[Users]

  private def tuple(user: User): UserTuple = {
    (user.id,
      user.name, user.emailLowerCase, user.token,
      user.settings.avatarUrl, user.settings.emailNotificationsEnabled,
      user.settings.dailyUpdatesEmailEnabled, user.settings.appTourDone,
      user.notifications.commits,
      user.notifications.followups)
  }

  private def tupleAuth(user: User): AuthTuple = tupleAuth(user.id, user.authentication)

  private def tupleAuth(id: ObjectId, auth: Authentication): AuthTuple = {
    (id,
      auth.provider, auth.username, auth.usernameLowerCase,
      auth.token, auth.salt)
  }

  private val untuple: ((UserTuple, AuthTuple)) => User = { case (tuple: UserTuple, authTuple: AuthTuple) =>
    val lastUserNotificationDispatch = LastUserNotificationDispatch(tuple._9, tuple._10)

    User(tuple._1,
      Authentication(authTuple._2, authTuple._3, authTuple._4, authTuple._5, authTuple._6),
      tuple._2, tuple._3, tuple._4,
      UserSettings(tuple._5, tuple._6, tuple._7, tuple._8),
      lastUserNotificationDispatch)
  }

  private def findOneWhere(condition: Users => Column[Boolean]): Option[User] = db.withTransaction { implicit session =>
    val q = for {
      u <- users if condition(u)
      a <- u.authJoin
    } yield (u, a)

    q.firstOption.map(untuple)
  }

  def schemas: List[JdbcProfile#DDLInvoker] = List(auths.ddl, users.ddl)
}