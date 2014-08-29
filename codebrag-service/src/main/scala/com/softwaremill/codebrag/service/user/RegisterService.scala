package com.softwaremill.codebrag.service.user

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.service.notification.NotificationService
import com.softwaremill.codebrag.dao.user.UserDAO
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
    userDao.add(user)
    doPostRegister(user)
    notificationService.sendWelcomeNotification(user)
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
