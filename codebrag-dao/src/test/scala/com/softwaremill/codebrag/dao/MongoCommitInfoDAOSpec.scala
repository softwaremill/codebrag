package com.softwaremill.codebrag.dao

import org.scalatest.matchers.ShouldMatchers
import org.joda.time.{Interval, DateTime}
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import CommitInfoAssembler._
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import com.softwaremill.codebrag.domain.CommitInfo

class MongoCommitInfoDAOSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers {
  var commitInfoDAO: MongoCommitInfoDAO = _
  val FixtureTime = new DateTime(23333333)
  override def beforeEach() {
    super.beforeEach()
    commitInfoDAO = new MongoCommitInfoDAO
  }

  it should "find a stored commit" taggedAs(RequiresDb) in {
    // given
    val commit = randomCommit.get
    commitInfoDAO.storeCommit(commit)

    // when
    val foundCommit = commitInfoDAO.findBySha(commit.sha)

    // then
    foundCommit should be(Some(commit.copy()))
  }

  it should "find stored commit by its id" taggedAs(RequiresDb) in {
    // given
    val commit = randomCommit.get
    commitInfoDAO.storeCommit(commit)

    // when
    val foundCommit = commitInfoDAO.findByCommitId(commit.id)

    // then
    foundCommit should be(Some(commit.copy()))
  }

  it should "store a single commit" taggedAs(RequiresDb) in {
    // given
    val commit = randomCommit.get

    // when
    commitInfoDAO.storeCommit(commit)

    // then
    commitInfoDAO.findBySha(commit.sha) should be('defined)
  }

  it should "return false in hasCommits when empty" taggedAs(RequiresDb) in {
    // given empty db

    // then
    commitInfoDAO.hasCommits should be(false)
  }

  it should "return true in hasCommits when not empty" taggedAs(RequiresDb) in {
    // given
    commitInfoDAO.storeCommit(randomCommit.get)

    // then
    commitInfoDAO.hasCommits should be(true)
  }

  it should "find no commits for interval not matching any item" in {
    // given
    givenCommitStoredOn(FixtureTime)
    givenCommitStoredOn(FixtureTime.plusMinutes(1))
    givenCommitStoredOn(FixtureTime.plusMinutes(2))

    // when
    val commits = commitInfoDAO.findForTimeRange(new Interval(FixtureTime.plusDays(1), FixtureTime.plusDays(2)))

    // then
    commits should be('empty)
  }

  it should "find commits within given time range" in {
    // given
    givenCommitStoredOn(FixtureTime)
    val commitAfterAMinute = givenCommitStoredOn(FixtureTime.plusMinutes(1))
    givenCommitStoredOn(FixtureTime.plusMinutes(20))
    val commitAfterTwoMinutes = givenCommitStoredOn(FixtureTime.plusMinutes(2))
    givenCommitStoredOn(FixtureTime.plusMinutes(30))

    // when
    val commits = commitInfoDAO.findForTimeRange(new Interval(FixtureTime.plusSeconds(20), FixtureTime.plusMinutes(3)))

    // then
    commits.size should be(2)
    commits(0) should equal(commitAfterAMinute)
    commits(1) should equal(commitAfterTwoMinutes)
  }

  it should "retrieve commit sha with last commit + author date" taggedAs(RequiresDb) in {
    // given
    val date = new DateTime()

    val expectedLastCommit = randomCommit.withAuthorDate(date.minusDays(2)).withCommitDate(date).get
    commitInfoDAO.storeCommit(randomCommit.withAuthorDate(date.minusDays(3)).withCommitDate(date).get)
    commitInfoDAO.storeCommit(randomCommit.withAuthorDate(date.minusHours(12)).withCommitDate(date.minusHours(13)).get)
    commitInfoDAO.storeCommit(expectedLastCommit)
    commitInfoDAO.storeCommit(randomCommit.withAuthorDate(date.minusDays(11)).withCommitDate(date).get)
    commitInfoDAO.storeCommit(randomCommit.withAuthorDate(date.minusHours(6)).withCommitDate(date.minusHours(8)).get)
    commitInfoDAO.storeCommit(randomCommit.withAuthorDate(date.minusHours(10)).withCommitDate(date.minusHours(11)).get)

    // when
    val lastSha = commitInfoDAO.findLastSha()

    // then
    lastSha should not be (null)
    lastSha should equal (Some(expectedLastCommit.sha))
  }

  it should "find all commits SHA" taggedAs(RequiresDb) in {
    // given
    val commits = List(CommitInfoAssembler.randomCommit.withSha("111").get, CommitInfoAssembler.randomCommit.withSha("222").get)
    commits.foreach {
      commitInfoDAO.storeCommit(_)
    }

    // when
    val commitsSha = commitInfoDAO.findAllSha()

    // then
    commitsSha should equal(commits.map(_.sha).toSet)
  }

  private def givenCommitStoredOn(dateTime: DateTime) = {
    val commit = randomCommit.withCommitDate(dateTime).get
    commitInfoDAO.storeCommit(commit)
    commit
  }

}
