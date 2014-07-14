package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.Utils
import org.joda.time.DateTime

/**
 * @param token Used by "remember me" - set in a cookie.
 */
case class User(id: ObjectId, authentication: Authentication, name: String, emailLowerCase: String, token: String,
  admin: Boolean, active: Boolean, settings: UserSettings, notifications: LastUserNotificationDispatch, aliases: UserAliases) {

  def makeAdmin = this.copy(admin = true)
}

object User {

  def apply(id: ObjectId, authentication: Authentication, name: String, emailLowerCase: String, token: String, admin: Boolean = false, active: Boolean = true) = {
    new User(id, authentication, name, emailLowerCase, token, admin, active, UserSettings.defaults(emailLowerCase), LastUserNotificationDispatch.defaults, UserAliases.defaults)
  }

  implicit object UserLikeRegularUser extends UserLike[User] {
    def userFullName(userLike: User) = userLike.name

    def userEmails(userLike: User) = Set(userLike.emailLowerCase)
  }

}

trait UserLike[T] {
  def userFullName(userLike: T): String

  def userEmails(userLike: T): Set[String]
}

case class Authentication(provider: String, username: String, usernameLowerCase: String, token: String, salt: String)

object Authentication extends ((String, String, String, String, String) => Authentication) {
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

object LastUserNotificationDispatch {
  def defaults = LastUserNotificationDispatch(None, None)
}

case class UserSettings(
  avatarUrl: String,
  emailNotificationsEnabled: Boolean,
  dailyUpdatesEmailEnabled: Boolean,
  appTourDone: Boolean,
  toReviewStartDate: Option[DateTime]
)

object UserSettings {

  def defaults(email: String) = {
    new UserSettings(defaultAvatarUrl(email), emailNotificationsEnabled = true, dailyUpdatesEmailEnabled = true, appTourDone = false, toReviewStartDate = None)
  }

  def defaultAvatarUrl(email: String): String = s"http://www.gravatar.com/avatar/${Utils.md5(email)}.png"

}


case class UserAlias(id: ObjectId, userId: ObjectId, alias: String)

object UserAlias {
  def apply(userId: ObjectId, alias: String) = new UserAlias(new ObjectId, userId, alias)
}

case class UserAliases(emailAliases: Set[UserAlias])

object UserAliases {
  def defaults = UserAliases(Set.empty)
}

