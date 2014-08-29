package com.softwaremill.codebrag.service.user

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import com.softwaremill.codebrag.service.notification.NotificationService
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.common.{ClockSpec, EventBus}
import com.softwaremill.codebrag.service.followups.{WelcomeFollowupsGenerator, FollowupsGeneratorForReactionsPriorUserRegistration}
import com.softwaremill.codebrag.dao.events.NewUserRegistered

class RegisterServiceSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfter with ClockSpec {

  val userDao = mock[UserDAO]
  val eventBus = mock[EventBus]
  val afterRegistered = mock[AfterUserRegistered]
  val notificationService = mock[NotificationService]
  val previousFollowupsGen = mock[FollowupsGeneratorForReactionsPriorUserRegistration]
  val welcomeFollowupsGen = mock[WelcomeFollowupsGenerator]

  val registerService = new RegisterService(userDao, eventBus, afterRegistered, notificationService, previousFollowupsGen, welcomeFollowupsGen)

  before {
    reset(userDao, eventBus, afterRegistered, notificationService, previousFollowupsGen, welcomeFollowupsGen)
  }

  it should "do post register actions after user registered" in {
    // given
    val user = UserAssembler.randomUser.get

    // when
    registerService.registerUser(user)

    // then
    val userRegiserteredEvent = NewUserRegistered(user)
    verify(afterRegistered).run(userRegiserteredEvent)
    verify(previousFollowupsGen).recreateFollowupsForPastComments(userRegiserteredEvent)
    verify(welcomeFollowupsGen).createWelcomeFollowupFor(userRegiserteredEvent)
  }

  it should "send welcome notification afer user registerd" in {
    // given
    val user = UserAssembler.randomUser.get

    // when
    registerService.registerUser(user)

    // then
    verify(notificationService).sendWelcomeNotification(user)
  }
}
