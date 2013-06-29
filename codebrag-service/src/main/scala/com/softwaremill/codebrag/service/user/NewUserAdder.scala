package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.common.EventBus
import com.softwaremill.codebrag.service.commits.CommitReviewTaskGeneratorActions
import com.softwaremill.codebrag.domain.User

class NewUserAdder(userDao: UserDAO,
                   eventBus: EventBus,
                   reviewTaskGenerator: CommitReviewTaskGeneratorActions) {

  def add(user: User) {
    userDao.add(user)
    val userRegisteredEvent = NewUserRegistered(user.id, user.authentication.usernameLowerCase, user.name, user.email)
    reviewTaskGenerator.handleNewUserRegistered(userRegisteredEvent)
    eventBus.publish(userRegisteredEvent)
  }
}
