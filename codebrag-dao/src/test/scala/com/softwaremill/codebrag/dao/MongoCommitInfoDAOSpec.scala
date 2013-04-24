package com.softwaremill.codebrag.dao

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import org.joda.time.DateTime
import CommitInfoAssembler._

class MongoCommitInfoDAOSpec extends FlatSpecWithMongo with BeforeAndAfterEach with ShouldMatchers {
  var commitInfoDAO: MongoCommitInfoDAO = _

  override def beforeEach() {
    CommitInfoRecord.drop // drop collection to start every test with fresh database
    commitInfoDAO = new MongoCommitInfoDAO
  }

  it should "find a stored commit" in {
    // given
    val commit = randomCommit.get
    commitInfoDAO.storeCommit(commit)

    // when
    val foundCommit = commitInfoDAO.findBySha(commit.sha)

    // then
    foundCommit should be(Some(commit.copy()))
  }

  it should "find stored commit by its id" in {
    // given
    val commit = randomCommit.get
    commitInfoDAO.storeCommit(commit)

    // when
    val foundCommit = commitInfoDAO.findByCommitId(commit.id)

    // then
    foundCommit should be(Some(commit.copy()))
  }

  it should "store a single commit" in {
    // given
    val commit = randomCommit.get

    // when
    commitInfoDAO.storeCommit(commit)

    // then
    commitInfoDAO.findBySha(commit.sha) should be('defined)
  }

  it should "retrieve commit with last author date" in {
    // given
    val date = new DateTime()
    val expectedLastCommit = randomCommit.withCommitDate(date.minusDays(2)).withDate(date).get
    commitInfoDAO.storeCommit(randomCommit.withCommitDate(date.minusHours(2)).withDate(date.minusHours(3)).get)
    commitInfoDAO.storeCommit(randomCommit.withCommitDate(date.minusHours(12)).withDate(date.minusHours(13)).get)
    commitInfoDAO.storeCommit(expectedLastCommit)
    commitInfoDAO.storeCommit(randomCommit.withCommitDate(date.minusHours(6)).withDate(date.minusHours(8)).get)
    commitInfoDAO.storeCommit(randomCommit.withCommitDate(date.minusHours(10)).withDate(date.minusHours(11)).get)

    // when
    val lastSha = commitInfoDAO.findLastSha()

    // then
    lastSha should not be (null)
    lastSha should equal (Some(expectedLastCommit.sha))
  }

  it should "find all commits SHA" in {
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

}
