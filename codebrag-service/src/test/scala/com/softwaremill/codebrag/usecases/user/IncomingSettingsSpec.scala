package com.softwaremill.codebrag.usecases.user

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.UserSettings
import org.joda.time.DateTime

class IncomingSettingsSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach {

  val existingSettings = UserSettings(
    avatarUrl =  "http://codebrag.com/avatar",
    dailyUpdatesEmailEnabled = true,
    emailNotificationsEnabled = true,
    appTourDone = false,
    toReviewStartDate = Some(DateTime.now)
  )

  it should "update only incoming settings" in {
    // given
    val noNotificationsSettings = newSettingsWithNotificationsDisabled
    val tourDoneSettings = newSettingsWithAppTourDone

    // when
    val updatedNotifications = noNotificationsSettings.applyTo(existingSettings)
    val updatedWelcomeFollowup = tourDoneSettings.applyTo(existingSettings)

    // then
    updatedNotifications should equal(existingSettings.copy(emailNotificationsEnabled = false, dailyUpdatesEmailEnabled = false))
    updatedWelcomeFollowup should equal(existingSettings.copy(appTourDone = true))
  }

  it should "update nothing when no values in incoming settings found" in {
    // given
    val emptySettings = IncomingSettings(None, None, None)

    // when
    val updatedSettings = emptySettings.applyTo(existingSettings)

    // then
    updatedSettings should equal(existingSettings)
  }

  val newSettingsWithNotificationsDisabled = IncomingSettings(
    emailNotificationsEnabled = Some(false),
    dailyUpdatesEmailEnabled = Some(false),
    appTourDone = None
  )

  val newSettingsWithAppTourDone = IncomingSettings(
    emailNotificationsEnabled = None,
    dailyUpdatesEmailEnabled = None,
    appTourDone = Some(true)
  )

}
