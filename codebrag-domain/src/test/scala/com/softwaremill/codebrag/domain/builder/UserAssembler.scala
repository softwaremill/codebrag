package com.softwaremill.codebrag.domain.builder

import com.softwaremill.codebrag.domain.{LastUserNotificationDispatch, UserSettings, Authentication, User}
import org.bson.types.ObjectId

class UserAssembler(var user: User) {

  def withFullName(name: String) = {
    user = user.copy(name = name)
    this
  }

  def withId(id: ObjectId) = {
    user = user.copy(id = id)
    this
  }

  def withAvatarUrl(url: String) = {
    user = user.copy(settings = UserSettings(url))
    this
  }

  def withEmail(email: String) = {
    user = user.copy(emailLowerCase = email)
    this
  }

  def withEmailNotificationsEnabled() = {
    user = user.copy(settings = user.settings.copy(emailNotificationsEnabled = true))
    this
  }

  def withEmailNotificationsDisabled() = {
    user = user.copy(settings = user.settings.copy(emailNotificationsEnabled = false))
    this
  }

  def withDailyDigestEmailDisabled() = {
    user = user.copy(settings = user.settings.copy(dailyUpdatesEmailEnabled = false))
    this
  }

  def withWelcomeFollowupNotYetDone() = {
    user = user.copy(settings = user.settings.copy(appTourDone = false))
    this
  }

  def get = user
}

object UserAssembler {
  def randomUser = new UserAssembler(createRandomUser())

  private def createRandomUser() = {
    User(new ObjectId, Authentication("Basic", "Sofokles", "sofokles", "token", "salt"), "Sofokles Mill", "sofo@sml.com", "token",
      UserSettings("http://avatar.com/1.jpg"), LastUserNotificationDispatch(None, None))
  }
}
