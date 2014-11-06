package com.softwaremill.codebrag.usecases.user

import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.UserSettings
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO

class ChangeUserSettingsUseCase(userDao: UserDAO) extends Logging {

  type ChangeUserSettingsResult = Either[String, UserSettings]

  def execute(implicit userId: ObjectId, newSettings: IncomingSettings): ChangeUserSettingsResult = {
    changeSettings(userId, newSettings)
  }

  private def changeSettings(userId: ObjectId, settings: IncomingSettings): ChangeUserSettingsResult = {
    userDao.findById(userId) match {
      case Some(userFound) => {
        val mergedSettings = settings.applyTo(userFound.settings)
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
}

case class IncomingSettings(emailNotificationsEnabled: Option[Boolean], dailyUpdatesEmailEnabled: Option[Boolean], appTourDone: Option[Boolean]) {

  def applyTo(existingSettings: UserSettings) = {
    existingSettings.copy(
      emailNotificationsEnabled = this.emailNotificationsEnabled.getOrElse(existingSettings.emailNotificationsEnabled),
      dailyUpdatesEmailEnabled = this.dailyUpdatesEmailEnabled.getOrElse(existingSettings.dailyUpdatesEmailEnabled),
      appTourDone = this.appTourDone.getOrElse(existingSettings.appTourDone)
    )
  }

}

