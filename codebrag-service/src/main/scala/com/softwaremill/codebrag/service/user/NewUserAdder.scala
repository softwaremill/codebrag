package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.common.EventBus
import com.softwaremill.codebrag.service.commits.CommitReviewTaskGeneratorActions
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.service.followups.WelcomeFollowupsGenerator

class NewUserAdder(userDao: UserDAO,
                   eventBus: EventBus,
                   reviewTaskGenerator: CommitReviewTaskGeneratorActions,
                    welcomeFollowupGenerator: WelcomeFollowupsGenerator) {

  def add(user: User):User = {
    val addedUser = userDao.add(user)
    val userRegisteredEvent = NewUserRegistered(addedUser)
    reviewTaskGenerator.handleNewUserRegistered(userRegisteredEvent)
    welcomeFollowupGenerator.createWelcomeFollowupFor(userRegisteredEvent)
    eventBus.publish(userRegisteredEvent)
    addedUser
  }
}
