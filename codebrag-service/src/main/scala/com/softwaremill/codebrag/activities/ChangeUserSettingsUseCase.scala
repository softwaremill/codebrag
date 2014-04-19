package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.service.data.UserJson
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.UserSettings
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO

class ChangeUserSettingsUseCase(userDao: UserDAO) extends Logging {

  def execute(user: UserJson, newSettings: IncomingSettings): Either[String, Unit] = {
    val userId = new ObjectId(user.id)
    userDao.findById(userId) match {
      case Some(userFound) => {
        val mergedSettings = newSettings.applyTo(userFound.settings)
        logger.debug(s"Updating settings for user ${userId} to ${mergedSettings}")
        userDao.changeUserSettings(userId, mergedSettings)
        Right()
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
    existingSettings
      .copy(emailNotificationsEnabled = this.emailNotificationsEnabled.getOrElse(existingSettings.emailNotificationsEnabled))
      .copy(dailyUpdatesEmailEnabled = this.dailyUpdatesEmailEnabled.getOrElse(existingSettings.dailyUpdatesEmailEnabled))
      .copy(appTourDone = this.appTourDone.getOrElse(existingSettings.appTourDone))
  }
}

