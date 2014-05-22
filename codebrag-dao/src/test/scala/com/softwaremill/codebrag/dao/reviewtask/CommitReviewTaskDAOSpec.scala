package com.softwaremill.codebrag.dao.reviewtask

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.CommitReviewTask
import org.bson.types.ObjectId
import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL}
import com.softwaremill.codebrag.dao.{RequiresDb, ObjectIdTestUtils}
import org.scalatest.FlatSpec
import com.softwaremill.codebrag.common.RealTimeClock

trait CommitReviewTaskDAOSpec extends FlatSpec with ShouldMatchers {

  def commitToReviewDao: CommitReviewTaskDAO

  val CommitId = ObjectIdTestUtils.oid(10)
  val UserId = ObjectIdTestUtils.oid(200)
  val OtherUserId = ObjectIdTestUtils.oid(300)

  it should "create commit to review for user" taggedAs RequiresDb in {
    // given

    // when
    commitToReviewDao.save(CommitReviewTask(CommitId, UserId))

    // then
    val storedCommitIds = commitToReviewDao.commitsPendingReviewFor(UserId)
    thereIsOneCommitToReviewStored(storedCommitIds)
    storedCommitIsCorrect(storedCommitIds.head)
  }

  it should "create only one commit to review record per commit and user pair" taggedAs RequiresDb in {
    // given
    storeReviewTaskFor(UserId, CommitId)

    // when
    commitToReviewDao.save(CommitReviewTask(CommitId, UserId))

    // then
    val storedCommitIds = commitToReviewDao.commitsPendingReviewFor(UserId)
    thereIsOneCommitToReviewStored(storedCommitIds)
  }

  it should "delete review task for given commit and user" taggedAs RequiresDb in {
    // given
    storeReviewTaskFor(UserId, CommitId)

    // when
    commitToReviewDao.delete(CommitReviewTask(CommitId, UserId))

    // then
    val storedCommitIds = commitToReviewDao.commitsPendingReviewFor(UserId)
    storedCommitIds should be('empty)
  }

  it should "not delete anything when no matching review found" taggedAs RequiresDb in {
    // given
    storeReviewTaskFor(OtherUserId, CommitId)

    // when
    commitToReviewDao.delete(CommitReviewTask(CommitId, UserId))

    // then
    val storedCommitIds = commitToReviewDao.commitsPendingReviewFor(OtherUserId)
    thereIsOneCommitToReviewStored(storedCommitIds)
    storedCommitIsCorrect(storedCommitIds.head)
  }


  def thereIsOneCommitToReviewStored(storedCommitIds: Set[ObjectId]) {
    storedCommitIds should have size (1)
  }

  def storeReviewTaskFor(userId: ObjectId, commitId: ObjectId) {
    commitToReviewDao.save(CommitReviewTask(commitId, userId))
  }

  def storedCommitIsCorrect(storedCommitId: ObjectId) {
    storedCommitId should equal(CommitId)
  }
}



class SQLCommitReviewTaskDAOSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with CommitReviewTaskDAOSpec {
  val commitToReviewDao = new SQLCommitReviewTaskDAO(sqlDatabase, RealTimeClock)
}
