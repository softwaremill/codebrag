package com.softwaremill.codebrag.dao

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.CommitReviewTask
import com.foursquare.rogue.LiftRogue._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.test.mongo.ClearMongoDataAfterTest

class MongoCommitReviewTaskDAOSpec extends FlatSpecWithMongo with ClearMongoDataAfterTest with ShouldMatchers {

  var commitToReviewDao: MongoCommitReviewTaskDAO = _

  val CommitId = ObjectIdTestUtils.oid(10)
  val UserId = ObjectIdTestUtils.oid(200)
  val OtherUserId = ObjectIdTestUtils.oid(300)

  override def beforeEach() {
    super.beforeEach()
    commitToReviewDao = new MongoCommitReviewTaskDAO
  }

  it should "create commit to review for user" taggedAs(RequiresDb) in {
    // given

    // when
    commitToReviewDao.save(CommitReviewTask(CommitId, UserId))

    // then
    val storedCommits = CommitReviewTaskRecord.findAll
    thereIsOneCommitToReviewStored(storedCommits)
    storedCommitIsCorrect(storedCommits.head)
  }

  it should "create only one commit to review record per commit and user pair" taggedAs(RequiresDb) in {
    // given
    storeReviewTaskFor(UserId, CommitId)

    // when
    commitToReviewDao.save(CommitReviewTask(CommitId, UserId))

    // then
    val storedCommits = CommitReviewTaskRecord.findAll
    thereIsOneCommitToReviewStored(storedCommits)
  }

  it should "delete review task for given commit and user" taggedAs(RequiresDb) in {
    // given
    storeReviewTaskFor(UserId, CommitId)

    // when
    commitToReviewDao.delete(CommitReviewTask(CommitId, UserId))

    // then
    val resultList = CommitReviewTaskRecord.where(_.commitId eqs CommitId).and(_.userId eqs UserId).fetch()
    resultList should be('empty)
  }

  it should "not delete anything when no matching review found" taggedAs(RequiresDb) in {
    // given
    storeReviewTaskFor(OtherUserId, CommitId)

    // when
    commitToReviewDao.delete(CommitReviewTask(CommitId, UserId))

    // then
    val resultList = CommitReviewTaskRecord.findAll
    resultList should have size(1)
    resultList.head.commitId.get should be(CommitId)
    resultList.head.userId.get should be(OtherUserId)
  }


  def thereIsOneCommitToReviewStored(storedCommits: List[CommitReviewTaskRecord]) {
    storedCommits should have size (1)
  }

  def storeReviewTaskFor(userId: ObjectId, commitId: ObjectId) {
    commitToReviewDao.save(CommitReviewTask(commitId, userId))
  }

  def storedCommitIsCorrect(storedCommit: CommitReviewTaskRecord) {
    storedCommit.commitId.get should equal(CommitId)
    storedCommit.userId.get should equal(UserId)
  }

}
