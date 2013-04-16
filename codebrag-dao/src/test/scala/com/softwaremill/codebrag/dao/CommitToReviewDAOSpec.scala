package com.softwaremill.codebrag.dao

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.CommitReviewTask

class CommitToReviewDAOSpec extends FlatSpecWithMongo with BeforeAndAfterEach with ShouldMatchers {

  var commitToReviewDao: MongoCommitReviewTaskDAO = _

  val CommitId = ObjectIdTestUtils.oid(10)
  val UserId = ObjectIdTestUtils.oid(200)

  override def beforeEach() {
    CommitReviewTaskRecord.drop
    commitToReviewDao = new MongoCommitReviewTaskDAO
  }

  it should "create commit to review for user" in {
    // given

    // when
    commitToReviewDao.save(CommitReviewTask(CommitId, UserId))

    // then
    val storedCommits = CommitReviewTaskRecord.findAll
    thereIsOneCommitToReviewStored(storedCommits)
    storedCommitIsCorrect(storedCommits.head)
  }

  it should "create only one commit to review record per commit and user pair" in {
    // given
    commitToReviewAlreadyStoredForUser()

    // when
    commitToReviewDao.save(CommitReviewTask(CommitId, UserId))

    // then
    val storedCommits = CommitReviewTaskRecord.findAll
    thereIsOneCommitToReviewStored(storedCommits)
  }


  def thereIsOneCommitToReviewStored(storedCommits: List[CommitReviewTaskRecord]) {
    storedCommits should have size (1)
  }

  def commitToReviewAlreadyStoredForUser() {
    commitToReviewDao.save(CommitReviewTask(CommitId, UserId))
  }

  def storedCommitIsCorrect(storedCommit: CommitReviewTaskRecord) {
    storedCommit.commitId.get should equal(CommitId)
    storedCommit.userId.get should equal(UserId)
  }

}
