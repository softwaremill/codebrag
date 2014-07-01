package com.softwaremill.codebrag.rest

import org.scalatra._
import com.softwaremill.codebrag.service.user.Authenticator
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.usecases.{IncomingSettings, ChangeUserSettingsUseCase}
import com.softwaremill.codebrag.domain.UserSettings

class UsersSettingsServlet(val authenticator: Authenticator, userDao: UserDAO, changeUserSettings: ChangeUserSettingsUseCase) extends JsonServletWithAuthentication with Logging {

  get("/") {
    haltIfNotAuthenticated()
    userDao.findById(user.idAsObjectId) match {
      case Some(user) => settingsResponse(user.settings)
      case None => NotFound
    }
  }

  put("/") {
    val newSettings = IncomingSettings(extractOpt[Boolean]("emailNotificationsEnabled"), extractOpt[Boolean]("dailyUpdatesEmailEnabled"), extractOpt[Boolean]("appTourDone"))
    changeUserSettings.execute(user.idAsObjectId, newSettings) match {
      case Right(settings) => Ok(settingsResponse(settings))
      case Left(err) => halt(400, Map("error" -> err))
    }
  }

  private def settingsResponse(settings: UserSettings) = Map("userSettings" -> user.settings)

}
