package com.softwaremill.codebrag.activities

import org.scalatest.{FlatSpec, BeforeAndAfterEach}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.common.{ClockSpec, EventBus}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{CommitInfo, CommitReviewTask}
import org.mockito.Mockito._
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.reactions.CommitReviewedEvent
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.reviewtask.CommitReviewTaskDAO

class CommitReviewActivitySpec
  extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach with ClockSpec {

  var commitReviewTaskDao: CommitReviewTaskDAO = _
  var commitInfoDao: CommitInfoDAO = _
  var eventBus: EventBus = _

  var activity: CommitReviewActivity = _

  override def beforeEach() {
    commitReviewTaskDao = mock[CommitReviewTaskDAO]
    commitInfoDao = mock[CommitInfoDAO]
    eventBus = mock[EventBus]

    activity = new CommitReviewActivity(commitReviewTaskDao, commitInfoDao, eventBus)
  }

  it should "generate commit reviewed event" in {
    // given
    val commitId = ObjectId.get
    val userId = ObjectId.get

    val task = CommitReviewTask(commitId, userId)
    val commit = CommitInfo("123456", "A commit", "Author Name", "author@sml.com", "Author Name", "author@sml.com", DateTime.now, DateTime.now, List())

    when(commitInfoDao.findByCommitId(commitId)).thenReturn(Some(commit))

    // when
    activity.markAsReviewed(task)

    // then
    verify(commitReviewTaskDao).delete(task)
    verify(eventBus).publish(CommitReviewedEvent(commit, userId))
  }


}
