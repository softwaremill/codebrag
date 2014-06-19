package com.softwaremill.codebrag.usecases

import org.scalatest.{FlatSpec, BeforeAndAfterEach}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.common.{ClockSpec, EventBus}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.ReviewedCommit
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.reactions.CommitReviewedEvent
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import com.softwaremill.codebrag.cache.UserReviewedCommitsCache
import com.softwaremill.codebrag.licence.LicenceService

class ReviewCommitUseCaseSpec
  extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach with ClockSpec {

  var reviewedCommitsCache: UserReviewedCommitsCache = _
  var commitInfoDao: CommitInfoDAO = _
  var licenceService: LicenceService = _
  var eventBus: EventBus = _

  var useCase: ReviewCommitUseCase = _

  val RepoName = "codebrag"

  override def beforeEach() {
    reviewedCommitsCache = mock[UserReviewedCommitsCache]
    commitInfoDao = mock[CommitInfoDAO]
    eventBus = mock[EventBus]
    licenceService = mock[LicenceService]
    useCase = new ReviewCommitUseCase(commitInfoDao, reviewedCommitsCache, eventBus, licenceService)
  }

  it should "generate commit reviewed event" in {
    // given
    val userId = ObjectId.get
    val commit = CommitInfoAssembler.randomCommit.get
    when(commitInfoDao.findBySha(RepoName, commit.sha)).thenReturn(Some(commit))

    // when
    useCase.execute(RepoName, commit.sha, userId)

    // then
    verify(eventBus).publish(CommitReviewedEvent(commit, userId))
  }

  it should "mark commit as reviewed if commit found" in {
    // given
    val userId = ObjectId.get
    val commit = CommitInfoAssembler.randomCommit.get
    when(commitInfoDao.findBySha(RepoName, commit.sha)).thenReturn(Some(commit))

    // when
    useCase.execute(RepoName, commit.sha, userId)

    // then
    val expectedCommitReviewed = ReviewedCommit(commit.sha, userId, clock.nowUtc)
    verify(reviewedCommitsCache).markCommitAsReviewed(expectedCommitReviewed)
  }

}
