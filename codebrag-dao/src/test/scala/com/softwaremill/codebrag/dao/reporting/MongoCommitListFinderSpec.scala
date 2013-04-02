package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.CommitInfo
import org.joda.time.DateTime
import ObjectIdTestUtils._


class MongoCommitListFinderSpec extends FlatSpecWithMongo with BeforeAndAfterEach with ShouldMatchers {

  val sampleCommit = CommitInfoBuilder.createRandomCommit(0)
  var commitListFinder: MongoCommitListFinder = _
  var commitInfoDAO: MongoCommitInfoDAO = _
  val FixtureDateTime: DateTime = new DateTime()
  val EmptyListOfParents = List.empty
  val EmptyListOfComments = List.empty
  val EmptyListOfFiles = List.empty

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

    val newerCommitTime: DateTime = FixtureDateTime.plusSeconds(5)
    val newestCommitTime: DateTime = FixtureDateTime.plusSeconds(10)
    val newerCommit = CommitInfo(oid(1), "123123123", "this is newer commit", "mostr", "mostr", newerCommitTime, EmptyListOfParents, EmptyListOfFiles)
    val newestCommit = CommitInfo(oid(2), "123123123", "this is newer commit2", "mostr", "mostr", newestCommitTime, EmptyListOfParents, EmptyListOfFiles)
    commitInfoDAO.storeCommit(newestCommit)
    commitInfoDAO.storeCommit(newerCommit)

    // when
    val pendingCommitList = commitListFinder.findAllPendingCommits()

    //then
    pendingCommitList.commits.length should equal (3)
    pendingCommitList.commits(0) should equal(CommitListItemDTO(oid(2).toString, "123123123", "this is newer commit2", "mostr", "mostr", newestCommitTime.toDate))
    pendingCommitList.commits(1) should equal(CommitListItemDTO(oid(1).toString, "123123123", "this is newer commit", "mostr", "mostr", newerCommitTime.toDate))
    pendingCommitList.commits(2) should equal(CommitListItemDTO(olderCommit.id.toString, olderCommit.sha, olderCommit.message,
      olderCommit.authorName, olderCommit.committerName, olderCommit.date.toDate))
  }

  it should "find empty list if no commits in database" in {
    // given
    CommitInfoRecord.drop

    // when
    val pendingCommitList = commitListFinder.findAllPendingCommits()

    //then
    pendingCommitList.commits should be ('empty)
  }
}
