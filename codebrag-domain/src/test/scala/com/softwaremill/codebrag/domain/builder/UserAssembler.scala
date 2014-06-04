package com.softwaremill.codebrag.domain.builder

import com.softwaremill.codebrag.domain.{LastUserNotificationDispatch, UserSettings, Authentication, User}
import org.bson.types.ObjectId
import org.joda.time.DateTime

class UserAssembler(var user: User) {

  def withFullName(name: String) = {
    user = user.copy(name = name)
    this
  }

  def withId(id: ObjectId) = {
    user = user.copy(id = id)
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

  def withDailyDigestEmailEnabled() = {
    user = user.copy(settings = user.settings.copy(dailyUpdatesEmailEnabled = true))
    this
  }

  def withWelcomeFollowupNotYetDone() = {
    user = user.copy(settings = user.settings.copy(appTourDone = false))
    this
  }

  def withSelectedBranch(branchName: String) = {
    user = user.copy(settings = user.settings.copy(selectedBranch= Some(branchName)))
    this
  }

  def withToReviewStartDate(date: DateTime) = {
    user = user.copy(settings = user.settings.copy(toReviewStartDate = Some(date)))
    this
  }

  def withNotificationsDispatch(notifs: LastUserNotificationDispatch) = {
    user = user.copy(notifications = notifs)
    this
  }

  def withBasicAuth(username: String, password: String) = {
    user = user.copy(authentication = Authentication.basic(username, password))
    this
  }

  def withToken(token: String) = {
    user = user.copy(token = token)
    this
  }

  def withAdmin(set: Boolean = true) = {
    user = user.copy(admin = set)
    this
  }

  def withActive(set: Boolean = true) = {
    user = user.copy(active = set)
    this
  }

  def get = user
}

object UserAssembler {
  def randomUser = new UserAssembler(createRandomUser())
  private def createRandomUser() = User(new ObjectId, Authentication("Basic", "Sofokles", "sofokles", "token", "salt"), "Sofokles Mill", "sofo@sml.com", "token")
}
