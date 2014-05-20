package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.common.{Clock, EventBus}
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.service.followups.{FollowupsGeneratorForReactionsPriorUserRegistration, WelcomeFollowupsGenerator}
import com.softwaremill.codebrag.dao.user.UserDAO

class NewUserAdder(userDao: UserDAO,
  eventBus: EventBus,
  afterRegistered: AfterUserRegistered,
  followupsForPriorReactionsGenerator: FollowupsGeneratorForReactionsPriorUserRegistration,
  welcomeFollowupGenerator: WelcomeFollowupsGenerator)(implicit clock: Clock) {

  def add(user: User):User = {
    val addedUser = userDao.add(user)
    val userRegisteredEvent = NewUserRegistered(addedUser)
    afterRegistered.run(userRegisteredEvent)
    followupsForPriorReactionsGenerator.recreateFollowupsForPastComments(userRegisteredEvent)
    welcomeFollowupGenerator.createWelcomeFollowupFor(userRegisteredEvent)
    eventBus.publish(userRegisteredEvent)
    addedUser
  }
}
