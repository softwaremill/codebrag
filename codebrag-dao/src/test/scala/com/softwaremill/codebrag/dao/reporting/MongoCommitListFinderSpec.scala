package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao.{MongoCommitInfoDAO, CommitInfoRecord, FlatSpecWithMongo}
import org.scalatest.{BeforeAndAfterEach, GivenWhenThen}
import org.scalatest.matchers.ShouldMatchers
import pl.softwaremill.common.util.RichString
import com.softwaremill.codebrag.domain.CommitInfo
import org.joda.time.DateTime

class MongoCommitListFinderSpec extends FlatSpecWithMongo with GivenWhenThen with BeforeAndAfterEach with ShouldMatchers {

  val sampleCommit = createCommit()
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
    Given("a sample commit and another one stored")
    val olderCommit = sampleCommit
    val newerCommit = CommitInfo("123123123", "this is newer commit", "mostr", "mostr", FixtureDateTime, List())
    val anotherNewerCommit = CommitInfo("123123123", "this is newer commit2", "mostr", "mostr", FixtureDateTime, List())
    commitInfoDAO.storeCommit(newerCommit)
    commitInfoDAO.storeCommit(anotherNewerCommit)

    When("trying to find all pending commits")
    val pendingCommitList = commitListFinder.findAllPendingCommits()

    Then("sample commit stored should be fetched")
    pendingCommitList.commits.length should equal (3)
    pendingCommitList.commits(0) should equal(CommitListItemDTO("123123123", "this is newer commit", "mostr", "mostr", FixtureDateTime.toDate))
    pendingCommitList.commits(1) should equal(CommitListItemDTO("123123123", "this is newer commit2", "mostr", "mostr", FixtureDateTime.toDate))
    pendingCommitList.commits(2) should equal(CommitListItemDTO(olderCommit.sha, olderCommit.message,
      olderCommit.authorName, olderCommit.committerName, olderCommit.date.toDate))
  }

  it should "find empty list if no commits in database" in {
    Given("empty database")
    CommitInfoRecord.drop

    When("trying to find all pending commits")
    val pendingCommitList = commitListFinder.findAllPendingCommits()

    Then("result list should be empty")
    pendingCommitList.commits should be ('empty)
  }

  def createCommit() = {
    val sha = RichString.generateRandom(10)
    val message = RichString.generateRandom(10)
    val authorName = RichString.generateRandom(10)
    val committerName = RichString.generateRandom(10)
    val parent = RichString.generateRandom(10)
    CommitInfo(sha, message, authorName, committerName, new DateTime(), List(parent))
  }

}
