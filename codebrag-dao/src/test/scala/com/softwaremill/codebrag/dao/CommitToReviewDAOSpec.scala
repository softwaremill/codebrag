package com.softwaremill.codebrag.dao

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.CommitToReview

class CommitToReviewDAOSpec extends FlatSpecWithMongo with BeforeAndAfterEach with ShouldMatchers {

  var commitToReviewDao: MongoCommitToReviewDAO = _

  val CommitId = ObjectIdTestUtils.oid(10)
  val UserId = ObjectIdTestUtils.oid(200)

  override def beforeEach() {
    CommitToReviewRecord.drop
    commitToReviewDao = new MongoCommitToReviewDAO
  }

  it should "create commit to review for user" in {
    // given

    // when
    commitToReviewDao.save(CommitToReview(CommitId, UserId))

    // then
    val storedCommits = CommitToReviewRecord.findAll
    thereIsOneCommitToReviewStored(storedCommits)
    storedCommitIsCorrect(storedCommits.head)
  }

  it should "create only one commit to review record per commit and user pair" in {
    // given
    commitToReviewAlreadyStoredForUser()

    // when
    commitToReviewDao.save(CommitToReview(CommitId, UserId))

    // then
    val storedCommits = CommitToReviewRecord.findAll
    thereIsOneCommitToReviewStored(storedCommits)
  }


  def thereIsOneCommitToReviewStored(storedCommits: List[CommitToReviewRecord]) {
    storedCommits should have size (1)
  }

  def commitToReviewAlreadyStoredForUser() {
    commitToReviewDao.save(CommitToReview(CommitId, UserId))
  }

  def storedCommitIsCorrect(storedCommit: CommitToReviewRecord) {
    storedCommit.commitId.get should equal(CommitId)
    storedCommit.userId.get should equal(UserId)
  }

}
