package com.softwaremill.codebrag.service.user

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.common.{ClockSpec, EventBus}
import com.softwaremill.codebrag.service.commits.CommitReviewTaskGenerator
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.service.followups.{FollowupsGeneratorForReactionsPriorUserRegistration, WelcomeFollowupsGenerator}
import com.softwaremill.codebrag.dao.user.UserDAO

class NewUserAdderTest
  extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach with ClockSpec {

  var welcomeFollowupGenerator: WelcomeFollowupsGenerator = _
  var userDao: UserDAO = _
  var eventBus: EventBus = _
  var reviewTaskGenerator: CommitReviewTaskGenerator = _
  var followupForPreviousReactionsGenerator: FollowupsGeneratorForReactionsPriorUserRegistration = _
  
  var userAdder: NewUserAdder = _

  override def beforeEach() {
    welcomeFollowupGenerator = mock[WelcomeFollowupsGenerator]
    userDao = mock[UserDAO]
    eventBus = mock[EventBus]
    reviewTaskGenerator = mock[CommitReviewTaskGenerator]
    followupForPreviousReactionsGenerator = mock[FollowupsGeneratorForReactionsPriorUserRegistration]
    userAdder = new NewUserAdder(userDao, eventBus, reviewTaskGenerator, followupForPreviousReactionsGenerator, welcomeFollowupGenerator)
  }

  it should "build new user event using registered user's data" in {
    // Given
    val user = UserAssembler.randomUser.get
    when(userDao.add(user)).thenReturn(user)

    // When
    userAdder.add(user)

    // Then
    val expectedNewUserEvent = NewUserRegistered(user)
    verify(eventBus).publish(expectedNewUserEvent)
  }
  
  it should "generate review tasks, welcome followups and followups for previous reactions for user" in {
    // Given
    val user = UserAssembler.randomUser.get
    when(userDao.add(user)).thenReturn(user)

    // When
    userAdder.add(user)

    // Then
    val newUserRegistered = NewUserRegistered(user)
    verify(reviewTaskGenerator).handleNewUserRegistered(newUserRegistered)
    verify(followupForPreviousReactionsGenerator).recreateFollowupsForPastComments(newUserRegistered)
    verify(welcomeFollowupGenerator).createWelcomeFollowupFor(newUserRegistered)
  }

}
