package com.softwaremill.codebrag.activities

import org.scalatest.{FlatSpec, BeforeAndAfterEach}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.common.{ClockSpec, EventBus}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{ReviewedCommit, CommitReviewTask}
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.reactions.CommitReviewedEvent
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.service.commits.branches.UserReviewedCommitsCache
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler

class CommitReviewActivitySpec
  extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach with ClockSpec {

  var reviewedCommitsCache: UserReviewedCommitsCache = _
  var commitInfoDao: CommitInfoDAO = _
  var eventBus: EventBus = _

  var activity: CommitReviewActivity = _

  override def beforeEach() {
    reviewedCommitsCache = mock[UserReviewedCommitsCache]
    commitInfoDao = mock[CommitInfoDAO]
    eventBus = mock[EventBus]

    activity = new CommitReviewActivity(commitInfoDao, reviewedCommitsCache, eventBus)
  }

  it should "generate commit reviewed event" in {
    // given
    val userId = ObjectId.get
    val commit = CommitInfoAssembler.randomCommit.get
    when(commitInfoDao.findByCommitId(commit.id)).thenReturn(Some(commit))

    // when
    activity.markAsReviewed(commit.id, userId)

    // then
    verify(eventBus).publish(CommitReviewedEvent(commit, userId))
  }

  it should "mark commit as reviewed if commit found" in {
    // given
    val userId = ObjectId.get
    val commit = CommitInfoAssembler.randomCommit.get
    when(commitInfoDao.findByCommitId(commit.id)).thenReturn(Some(commit))

    // when
    activity.markAsReviewed(commit.id, userId)

    // then
    val expectedCommitReviewed = ReviewedCommit(commit.sha, userId, clock.nowUtc)
    verify(reviewedCommitsCache).markCommitAsReviewed(expectedCommitReviewed)
  }

}
