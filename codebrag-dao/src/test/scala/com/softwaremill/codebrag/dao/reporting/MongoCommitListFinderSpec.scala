package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao.{CommitInfoBuilder, MongoCommitInfoDAO, CommitInfoRecord, FlatSpecWithMongo}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.CommitInfo
import org.joda.time.DateTime
import org.bson.types.ObjectId

class MongoCommitListFinderSpec extends FlatSpecWithMongo with BeforeAndAfterEach with ShouldMatchers {

  val sampleCommit = CommitInfoBuilder.createRandomCommit()
  var commitListFinder: MongoCommitListFinder = _
  var commitInfoDAO: MongoCommitInfoDAO = _
  val FixtureDateTime: DateTime = new DateTime()

  override def beforeEach() {
    CommitInfoRecord.drop // drop collection to start every test with fresh database
    commitInfoDAO = new MongoCommitInfoDAO
    commitListFinder = new MongoCommitListFinder
    commitInfoDAO.storeCommit(sampleCommit)
  }

  behavior of "MongoCommitListFinder"

  it should "find all pending commits starting from newest" in {
    // given
    val olderCommit = sampleCommit
    val newerCommit = CommitInfo(new ObjectId(), "123123123", "this is newer commit", "mostr", "mostr", FixtureDateTime, List.empty, List.empty)
    val anotherNewerCommit = CommitInfo(new ObjectId(), "123123123", "this is newer commit2", "mostr", "mostr", FixtureDateTime, List.empty, List.empty)
    commitInfoDAO.storeCommit(newerCommit)
    commitInfoDAO.storeCommit(anotherNewerCommit)

    // when
    val pendingCommitList = commitListFinder.findAllPendingCommits()

    // then
    pendingCommitList.commits.length should equal (3)
    pendingCommitList.commits(0) should equal(CommitListItemDTO("123123123", "this is newer commit", "mostr", "mostr", FixtureDateTime.toDate))
    pendingCommitList.commits(1) should equal(CommitListItemDTO("123123123", "this is newer commit2", "mostr", "mostr", FixtureDateTime.toDate))
    pendingCommitList.commits(2) should equal(CommitListItemDTO(olderCommit.sha, olderCommit.message,
      olderCommit.authorName, olderCommit.committerName, olderCommit.date.toDate))
  }

  it should "find empty list if no commits in database" in {
    // given
    CommitInfoRecord.drop

    // when
    val pendingCommitList = commitListFinder.findAllPendingCommits()

    // then
    pendingCommitList.commits should be ('empty)
  }


}
