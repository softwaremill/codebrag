package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.Utils
import org.joda.time.DateTime

case class User(id: ObjectId, authentication: Authentication, name: String, email: String, token: String, settings: UserSettings,
                notifications: LastUserNotificationDispatch)

object User {
  def apply(authentication: Authentication, name: String, email: String, token: String, avatarUrl: String) = {
    new User(null, authentication, name, email, token, UserSettings(avatarUrl), LastUserNotificationDispatch(None, None))
  }

  def apply(id: ObjectId, authentication: Authentication, name: String, email: String, token: String, avatarUrl: String) = {
    new User(id, authentication, name, email, token, UserSettings(avatarUrl), LastUserNotificationDispatch(None, None))
  }

  def apply(id: ObjectId, authentication: Authentication, name: String, email: String, token: String, settings: UserSettings) = {
    new User(id, authentication, name, email, token, settings, LastUserNotificationDispatch(None, None))
  }

  implicit object UserLikeRegularUser extends UserLike[User] {
    def userFullName(userLike: User) = userLike.name

    def userEmail(userLike: User) = userLike.email
  }

}

trait UserLike[T] {
  def userFullName(userLike: T): String

  def userEmail(userLike: T): String
}

case class Authentication(provider: String, username: String, usernameLowerCase: String, token: String, salt: String)

object Authentication {
  def github(username: String, accessToken: String) = {
    Authentication("GitHub", username, username.toLowerCase, accessToken, "")
  }

  def basic(username: String, password: String) = {
    val salt = Utils.randomString(16)
    Authentication("Basic", username, username.toLowerCase, encryptPassword(password, salt), salt)
  }

  def encryptPassword(password: String, salt: String): String = {
    Utils.sha256(password, salt)
  }

  def passwordsMatch(plainPassword: String, authentication: Authentication): Boolean = {
    authentication.token.equals(encryptPassword(plainPassword, authentication.salt))
  }

}

case class LastUserNotificationDispatch(commits: Option[DateTime], followups: Option[DateTime])

case class UserSettings(avatarUrl: String, emailNotificationsEnabled: Boolean, dailyUpdatesEmailEnabled: Boolean, appTourDone: Boolean)

object UserSettings {

  def apply(avatarUrl: String) = new UserSettings(avatarUrl, emailNotificationsEnabled = true, dailyUpdatesEmailEnabled = true, appTourDone = false)

  def defaultAvatarUrl(email: String): String = {
    s"http://www.gravatar.com/avatar/${Utils.md5(email)}.png"
  }
}

