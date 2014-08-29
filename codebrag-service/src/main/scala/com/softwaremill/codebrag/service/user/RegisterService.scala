package com.softwaremill.codebrag.service.user

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.{LastUserNotificationDispatch, UserSettings, User, Authentication}
import java.util.UUID
import com.softwaremill.codebrag.service.invitations.InvitationService
import com.softwaremill.codebrag.service.notification.NotificationService
import com.softwaremill.codebrag.dao.user.UserDAO
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.common.{Clock, EventBus}
import com.softwaremill.codebrag.service.followups.{WelcomeFollowupsGenerator, FollowupsGeneratorForReactionsPriorUserRegistration}

class RegisterService(
  userDao: UserDAO,
  eventBus: EventBus,
  afterRegistered: AfterUserRegistered,
  notificationService: NotificationService,
  followupsForPriorReactionsGenerator: FollowupsGeneratorForReactionsPriorUserRegistration,
  welcomeFollowupGenerator: WelcomeFollowupsGenerator)(implicit clock: Clock) extends Logging {

  def registerUser(user: User) = {
    logger.info(s"Trying to register user: $user.name")
    val addedUser = userDao.add(user) // TODO: fix, no need to return, user is ready
    doPostRegister(addedUser)
    notificationService.sendWelcomeNotification(addedUser)
  }

  def isFirstRegistration = userDao.countAll() == 0

  private def doPostRegister(addedUser: User) {
    val userRegisteredEvent = NewUserRegistered(addedUser)
    afterRegistered.run(userRegisteredEvent)
    followupsForPriorReactionsGenerator.recreateFollowupsForPastComments(userRegisteredEvent)
    welcomeFollowupGenerator.createWelcomeFollowupFor(userRegisteredEvent)
    eventBus.publish(userRegisteredEvent)
  }
}
