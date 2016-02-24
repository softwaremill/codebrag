package com.softwaremill.codebrag.usecases.reactions

import com.softwaremill.codebrag.common.{ClockSpec, EventBus}
import com.softwaremill.codebrag.domain.reactions.AllCommitsReviewedEvent
import org.bson.types.ObjectId
import org.mockito.Mockito._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, FlatSpec}

class ReviewAllCommitsUseCaseSpec
  extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach with ClockSpec {

  var eventBus: EventBus = _

  var useCase: ReviewAllCommitsUseCase = _

  val RepoName = "codebrag"
  val BranchName = "master"

  override def beforeEach() {
    eventBus = mock[EventBus]
    useCase = new ReviewAllCommitsUseCase(eventBus)
  }

  it should "generate all commits reviewed event" in {
    // given
    val userId = ObjectId.get

    // when
    useCase.execute(RepoName, BranchName, userId)

    // then
    verify(eventBus).publish(AllCommitsReviewedEvent(RepoName, BranchName, userId))
  }


}
