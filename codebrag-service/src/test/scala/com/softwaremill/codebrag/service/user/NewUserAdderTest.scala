package com.softwaremill.codebrag.service.user

import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.dao.UserDAO
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.common.EventBus
import com.softwaremill.codebrag.service.commits.CommitReviewTaskGeneratorActions
import org.mockito.ArgumentCaptor
import com.softwaremill.codebrag.dao.events.NewUserRegistered

class NewUserAdderTest extends FlatSpec with MockitoSugar with ShouldMatchers {
  it should "use the generated id when invoking the event" in {
    // Given
    val mockUser = mock[User]
    val savedUser = UserAssembler.randomUser.get

    val mockUserDao = mock[UserDAO]
    when(mockUserDao.add(mockUser)).thenReturn(savedUser)

    val mockEventBus = mock[EventBus]
    val mockCommitReviewTaskGeneratorActions = mock[CommitReviewTaskGeneratorActions]

    // When
    new NewUserAdder(mockUserDao, mockEventBus, mockCommitReviewTaskGeneratorActions).add(mockUser)

    // Then
    val eventCaptor = ArgumentCaptor.forClass(classOf[NewUserRegistered])
    verify(mockEventBus).publish(eventCaptor.capture())

    eventCaptor.getValue.id should be (savedUser.id)
  }
}
