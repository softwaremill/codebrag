package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.domain._
import com.softwaremill.codebrag.domain.LastUserNotificationDispatch
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.sql.{WithSQLSchema, SQLDatabase}
import scala.slick.driver.JdbcProfile
import org.joda.time.DateTime

class SQLUserDAO(db: SQLDatabase) extends UserDAO with WithSQLSchema {
  import db.driver.simple._
  import db.db._

  def add(user: User) = {
    // TODO -> move to trait?
    val toSave = if (user.id == null) {
      user.copy(id = new ObjectId())
    } else user

    withTransaction { implicit session =>
      users += tuple(toSave)
    }

    toSave
  }

  def findAll() = withTransaction { implicit session =>
    users.list().map(untuple)
  }

  def findById(userId: ObjectId) = findOneWhere(_.id === userId.toString)

  def findByEmail(email: String) = findOneWhere(_.email === email.toLowerCase) // TODO: extract the common to-lower-case

  def findByLowerCasedLogin(login: String) = findOneWhere(_.authUsernameLower === login.toLowerCase)

  def findByLoginOrEmail(login: String, email: String) = findOneWhere { u =>
    u.authUsernameLower === login.toLowerCase || u.email === email.toLowerCase
  }

  def findByToken(token: String) = findOneWhere(_.token === token)

  def findCommitAuthor(commit: CommitInfo) = findOneWhere { u =>
    u.name === commit.authorName || u.email === commit.authorEmail
  }

  def changeAuthentication(id: ObjectId, auth: Authentication) = withTransaction { implicit session =>
    val q = for {
      u <- users if u.id === id.toString
    } yield (u.authProvider, u.authUsername, u.authUsernameLower, u.authToken, u.authSalt)

    q.update(auth.provider, auth.username, auth.usernameLowerCase, auth.token, auth.salt)
  }

  def rememberNotifications(id: ObjectId, notifications: LastUserNotificationDispatch) = withTransaction { implicit session =>
    val q = for {
      u <- users if u.id === id.toString
    } yield (u.notifLastCommitsDispatch, u.notifLastFollowupsDispatch)

    q.update(toSqlDate(notifications.commits), toSqlDate(notifications.followups))
  }

  def changeUserSettings(id: ObjectId, newSettings: UserSettings) = withTransaction { implicit session =>
    val q = for {
      u <- users if u.id === id.toString
    } yield (u.settingsAvatarUrl, u.settingsEmailNotificationsEnabled, u.settingsDailyUpdatesEmailEnabled, u.settingsAppTourDone)

    q.update(newSettings.avatarUrl, newSettings.emailNotificationsEnabled,
      newSettings.dailyUpdatesEmailEnabled, newSettings.appTourDone)
  }

  private type SQLDateTime = java.sql.Timestamp
  private type UserTuple = (String, String, String, String, String, String, String, String, String, String, Boolean, Boolean, Boolean, Option[SQLDateTime], Option[SQLDateTime])

  // TODO: extract entities
  private class Users(tag: Tag) extends Table[UserTuple](tag, "users") {
    def id = column[String]("id", O.PrimaryKey)
    def authProvider = column[String]("auth_provider")
    def authUsername = column[String]("auth_username")
    def authUsernameLower = column[String]("auth_username_lower")
    def authToken = column[String]("auth_token")
    def authSalt = column[String]("auth_salt")
    def name = column[String]("name")
    def email = column[String]("email")
    def token = column[String]("token") // TODO: why two tokens
    def settingsAvatarUrl = column[String]("settings_avatar_url")
    def settingsEmailNotificationsEnabled = column[Boolean]("settings_email_notif")
    def settingsDailyUpdatesEmailEnabled = column[Boolean]("settings_email_daily_updates")
    def settingsAppTourDone = column[Boolean]("settings_app_tour_done")
    def notifLastCommitsDispatch = column[Option[SQLDateTime]]("notif_last_commits_dispatch")
    def notifLastFollowupsDispatch = column[Option[SQLDateTime]]("notif_last_followups_dispatch")

    def * = (id, authProvider, authUsername, authUsernameLower, authToken, authSalt, name, email, token,
      settingsAvatarUrl, settingsEmailNotificationsEnabled, settingsDailyUpdatesEmailEnabled, settingsAppTourDone,
      notifLastCommitsDispatch, notifLastFollowupsDispatch)
  }

  private val users = TableQuery[Users]

  // TODO: custom type?
  private implicit def toSqlDate(dt: DateTime) = new SQLDateTime(dt.getMillis)
  private implicit def toSqlDate(dto: Option[DateTime]) = dto.map(dt => new SQLDateTime(dt.getMillis))

  private def tuple(user: User): UserTuple = {
    (user.id.toString,
      user.authentication.provider, user.authentication.username, user.authentication.usernameLowerCase,
      user.authentication.token, user.authentication.salt,
      user.name, user.email, user.token,
      user.settings.avatarUrl, user.settings.emailNotificationsEnabled,
      user.settings.dailyUpdatesEmailEnabled, user.settings.appTourDone,
      user.notifications.commits,
      user.notifications.followups)
  }

  private def untuple(tuple: UserTuple): User = {
    val lastUserNotificationDispatch = LastUserNotificationDispatch(
      tuple._14.map(d => new DateTime(d.getTime)),
      tuple._15.map(d => new DateTime(d.getTime)))

    User(new ObjectId(tuple._1),
      Authentication(tuple._2, tuple._3, tuple._4, tuple._5, tuple._6),
      tuple._7, tuple._8, tuple._9,
      UserSettings(tuple._10, tuple._11, tuple._12, tuple._13),
      lastUserNotificationDispatch)
  }

  private def findOneWhere(condition: Users => Column[Boolean]): Option[User] = {
    withTransaction { implicit session =>
      val q = for {
        u <- users if condition(u)
      } yield u

      q.firstOption.map(untuple)
    }
  }

  def schema: JdbcProfile#DDLInvoker = users.ddl
}