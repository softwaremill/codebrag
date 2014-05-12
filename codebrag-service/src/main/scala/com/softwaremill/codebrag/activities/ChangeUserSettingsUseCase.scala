package com.softwaremill.codebrag.activities

import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.UserSettings
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.licence.LicenceService

class ChangeUserSettingsUseCase(userDao: UserDAO, licenceService: LicenceService) extends Logging {

  type ChangeUserSettingsResult = Either[String, UserSettings]

  def execute(implicit userId: ObjectId, newSettings: IncomingSettings): ChangeUserSettingsResult = {
    ifCanExecute {
      changeSettings(userId, newSettings)
    }
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

  protected def ifCanExecute(block: => ChangeUserSettingsResult)(implicit userId: ObjectId, settings: IncomingSettings): ChangeUserSettingsResult = {
    licenceService.interruptIfLicenceExpired()
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

