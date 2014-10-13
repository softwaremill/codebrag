package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.UserSettings
import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.usecases.user.{ChangeUserSettingsUseCase, IncomingSettings}
import com.typesafe.scalalogging.slf4j.Logging
import org.scalatra._

class UsersSettingsServlet(val authenticator: Authenticator, userDao: UserDAO, changeUserSettings: ChangeUserSettingsUseCase) extends JsonServletWithAuthentication with Logging {

  get("/") {
    haltIfNotAuthenticated()
    userDao.findById(user.id).map(u => asResponseKey(u.settings))
  }

  put("/") {
    val newSettings = IncomingSettings(
      extractOpt[Boolean]("emailNotificationsEnabled"),
      extractOpt[Boolean]("dailyUpdatesEmailEnabled"),
      extractOpt[Boolean]("appTourDone")
    )
    changeUserSettings.execute(user.id, newSettings).right.map(asResponseKey)
  }

  private def asResponseKey(settings: UserSettings) = Map("userSettings" -> user.settings)

}
