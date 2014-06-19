package com.softwaremill.codebrag.usecases

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
    toReviewStartDate = Some(DateTime.now),
    selectedBranch = None
  )

  it should "update only incoming settings" in {
    // given
    val noNotificationsSettings = newSettingsWithNotificationsDisabled
    val tourDoneSettings = newSettingsWithAppTourDone
    val selectedBranchSettings = newSettingsWithBranchSelected

    // when
    val updatedNotifications = noNotificationsSettings.applyTo(existingSettings)
    val updatedWelcomeFollowup = tourDoneSettings.applyTo(existingSettings)
    val updatedBranchSettings = selectedBranchSettings.applyTo(existingSettings)

    // then
    updatedNotifications should equal(existingSettings.copy(emailNotificationsEnabled = false, dailyUpdatesEmailEnabled = false))
    updatedWelcomeFollowup should equal(existingSettings.copy(appTourDone = true))
    updatedBranchSettings should equal(existingSettings.copy(selectedBranch = Some("master")))
  }

  it should "update nothing when no values in incoming settings found" in {
    // given
    val emptySettings = IncomingSettings(None, None, None, None)

    // when
    val updatedSettings = emptySettings.applyTo(existingSettings)

    // then
    updatedSettings should equal(existingSettings)
  }

  val newSettingsWithNotificationsDisabled = IncomingSettings(
    emailNotificationsEnabled = Some(false),
    dailyUpdatesEmailEnabled = Some(false),
    appTourDone = None,
    newBranch = None)

  val newSettingsWithAppTourDone = IncomingSettings(
    emailNotificationsEnabled = None,
    dailyUpdatesEmailEnabled = None,
    appTourDone = Some(true),
    newBranch = None)

  val newSettingsWithBranchSelected = IncomingSettings(
    emailNotificationsEnabled = None,
    dailyUpdatesEmailEnabled = None,
    appTourDone = Some(false),
    newBranch = Some("master"))
}
