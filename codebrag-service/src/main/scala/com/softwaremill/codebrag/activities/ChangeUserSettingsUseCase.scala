package com.softwaremill.codebrag.activities

import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.UserSettings
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO

class ChangeUserSettingsUseCase(userDao: UserDAO) extends Logging {

  type ChangeUserSettingsResult = Either[String, UserSettings]

  def execute(userId: ObjectId, newSettings: IncomingSettings): ChangeUserSettingsResult = {
    userDao.findById(userId) match {
      case Some(userFound) => {
        val mergedSettings = newSettings.applyTo(userFound.settings)
        logger.debug(s"Updating settings for user ${userId} to ${mergedSettings}")
        userDao.changeUserSettings(userId, mergedSettings)
        Right(mergedSettings)
      }
      case None => {
        logger.debug(s"Could not find user ${userId}")
        Left("User not found")
      }
    }
  }

  protected def ifCanExecute(block: => ChangeUserSettingsResult)(implicit userId: ObjectId, settings: IncomingSettings): ChangeUserSettingsResult = {
    block
  }

}

case class IncomingSettings(emailNotificationsEnabled: Option[Boolean], dailyUpdatesEmailEnabled: Option[Boolean], appTourDone: Option[Boolean]) {
  def applyTo(existingSettings: UserSettings) = {
    existingSettings
      .copy(emailNotificationsEnabled = this.emailNotificationsEnabled.getOrElse(existingSettings.emailNotificationsEnabled))
      .copy(dailyUpdatesEmailEnabled = this.dailyUpdatesEmailEnabled.getOrElse(existingSettings.dailyUpdatesEmailEnabled))
      .copy(appTourDone = this.appTourDone.getOrElse(existingSettings.appTourDone))
  }
}

