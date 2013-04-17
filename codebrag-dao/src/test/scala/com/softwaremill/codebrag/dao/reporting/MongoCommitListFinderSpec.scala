package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.CommitInfo
import org.joda.time.DateTime
import ObjectIdTestUtils._
import scala.collection.immutable.IndexedSeq
import org.bson.types.ObjectId


class MongoCommitListFinderSpec extends FlatSpecWithMongo with BeforeAndAfterEach with ShouldMatchers {

  val commitListFinder = new MongoCommitListFinder
  var commitReviewTaskDao = new  MongoCommitReviewTaskDAO
  val commitInfoDao = new MongoCommitInfoDAO

  val userId = ObjectIdTestUtils.oid(123)

  override def beforeEach() {
    CommitInfoRecord.drop // drop collection to start every test with fresh database
  }

  it should "find all commits to review for given user only" in {
    // given
    val storedCommits = prepareAndStoreSomeCommits(howMany = 5)
    storeCommitReviewTasksFor(userId, storedCommits(0), storedCommits(1))

    // when
    val commitsFound = commitListFinder.findCommitsToReviewForUser(userId)

    // then
    commitsFound.commits should have size(2)
  }

  it should "find commits starting from newest" in {
    // given
    val baseDate = DateTime.now()
    val olderCommit = CommitInfoAssembler.randomCommit.withSha("111").withDate(baseDate).get
    val newerCommit = CommitInfoAssembler.randomCommit.withSha("222").withDate(baseDate.plusSeconds(10)).get
    commitInfoDao.storeCommit(newerCommit)
    commitInfoDao.storeCommit(olderCommit)
    storeCommitReviewTasksFor(userId, olderCommit, newerCommit)

    // when
    val pendingCommitList = commitListFinder.findCommitsToReviewForUser(userId)

    //then
    pendingCommitList.commits.length should equal (2)
    pendingCommitList.commits(0).sha should equal(newerCommit.sha)
    pendingCommitList.commits(1).sha should equal(olderCommit.sha)
  }

  it should "find empty list if there are no commits to review for user" in {
    // given
    prepareAndStoreSomeCommits(5)

    // when
    val pendingCommitList = commitListFinder.findCommitsToReviewForUser(userId)

    //then
    pendingCommitList.commits should be ('empty)
  }

  def prepareAndStoreSomeCommits(howMany: Int) = {
    val commitsPrepared = (1 to howMany).map{ i => CommitInfoAssembler.randomCommit.withSha(i.toString).get }
    commitsPrepared.foreach{ commitInfoDao.storeCommit(_) }
    commitsPrepared.toList
  }

  def storeCommitReviewTasksFor(userId: ObjectId, commits: CommitInfo*) {
    commits.foreach{commit =>
      commit.createReviewTasksFor(List(userId)).foreach{ commitReviewTaskDao.save(_)}
    }
  }

}
