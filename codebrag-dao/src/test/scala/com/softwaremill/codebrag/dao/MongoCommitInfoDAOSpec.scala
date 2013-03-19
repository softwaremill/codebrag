package com.softwaremill.codebrag.dao

import org.scalatest.{BeforeAndAfterAll, GivenWhenThen}
import com.softwaremill.codebrag.domain.CommitInfo
import org.scalatest.matchers.ShouldMatchers
import pl.softwaremill.common.util.RichString

class MongoCommitInfoDAOSpec extends FlatSpecWithMongo with GivenWhenThen with BeforeAndAfterAll with ShouldMatchers {
  val sampleCommit = createCommit()
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
    val commit = createCommit()

    When("trying to store it")
    commitInfoDAO.storeCommit(commit)

    Then("it is stored")
    commitInfoDAO.findBySha(commit.sha) should be('defined)
  }

  it should "store a collection of commits" in {
    Given("a collection of commits")
    val commit1 = createCommit()
    val commit2 = createCommit()
    val commits = Seq[CommitInfo](commit1, commit2)

    When("trying to store them")
    commitInfoDAO.storeCommits(commits)

    Then("they are stored")
    commitInfoDAO.findBySha(commit1.sha) should be(Some(commit1))
    commitInfoDAO.findBySha(commit2.sha) should be(Some(commit2))
  }

  it should "find all commits pending review" in {
    Given("a sample commit and another one stored")
    val anotherCommit = createCommit()
    commitInfoDAO.storeCommit(anotherCommit)

    When("trying to find all stored commits")
    val pendingCommits = commitInfoDAO.findAllPendingCommits()

    Then("sample commit stored should be fetched")
    pendingCommits should be equals(Seq(sampleCommit, anotherCommit))
  }

  def createCommit() = {
    val sha = RichString.generateRandom(10)
    val message = RichString.generateRandom(10)
    val authorName = RichString.generateRandom(10)
    val committerName = RichString.generateRandom(10)
    CommitInfo(sha, message, authorName, committerName)
  }

}
