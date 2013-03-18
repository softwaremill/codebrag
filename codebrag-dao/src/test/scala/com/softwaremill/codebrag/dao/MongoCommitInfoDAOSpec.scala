package com.softwaremill.codebrag.dao

import org.scalatest.{BeforeAndAfterAll, GivenWhenThen}
import com.softwaremill.codebrag.domain.CommitInfo
import org.scalatest.matchers.ShouldMatchers

class MongoCommitInfoDAOSpec extends FlatSpecWithMongo with GivenWhenThen with BeforeAndAfterAll with ShouldMatchers {
  val sampleCommit = CommitInfo("sha1")
  var commitInfoDAO: MongoCommitInfoDAO = _

  override def beforeAll() {
    super.beforeAll()

    commitInfoDAO = new MongoCommitInfoDAO

    commitInfoDAO.storeCommit(sampleCommit)
  }

  behavior of "MongoCommitInfoDAO"

  it should "find a stored commit" in {
    Given("a stored commit")

    When("searching for it")
    val commit = commitInfoDAO.findBySha(sampleCommit.sha)

    Then("it is found")
    commit should be(Some(sampleCommit.copy()))
  }

  it should "store a single commit" in {
    Given("a commit")
    val commit = CommitInfo("sha")

    When("trying to store it")
    commitInfoDAO.storeCommit(commit)

    Then("it is stored")
    commitInfoDAO.findBySha("sha") should be('defined)
  }

  it should "store a collection of commits" in {
    Given("a collection of commits")
    val commits = Seq[CommitInfo](CommitInfo("a"), CommitInfo("b"))

    When("trying to store them")
    commitInfoDAO.storeCommits(commits)

    Then("they are stored")
    commitInfoDAO.findBySha("a") should be(Some(CommitInfo("a")))
    commitInfoDAO.findBySha("b") should be(Some(CommitInfo("b")))
  }

}
