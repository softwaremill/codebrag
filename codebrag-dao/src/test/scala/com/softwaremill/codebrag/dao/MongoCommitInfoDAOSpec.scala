package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.CommitInfo
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, GivenWhenThen}
import org.scalatest.matchers.ShouldMatchers
import pl.softwaremill.common.util.RichString
import org.joda.time.DateTime

class MongoCommitInfoDAOSpec extends FlatSpecWithMongo with GivenWhenThen with BeforeAndAfterEach with ShouldMatchers {
  val sampleCommit = createCommit()
  var commitInfoDAO: MongoCommitInfoDAO = _

  override def beforeEach() {
    CommitInfoRecord.drop // drop collection to start every test with fresh database
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

  it should "find all commits starting from newest" in {
    Given("a sample commit and another one stored")
    val olderCommit = sampleCommit
    val newerCommit = CommitInfo("123123123", "this is newer commit", "mostr", "mostr", new DateTime(), List())
    val anotherNewerCommit = CommitInfo("123123123", "this is newer commit", "mostr", "mostr", new DateTime(), List())
    commitInfoDAO.storeCommit(newerCommit)

    When("trying to find all stored commits")
    val commits = commitInfoDAO.findAll()

    Then("sample commit stored should be fetched")
    commits(0) should equal(newerCommit)
    commits(1) should equal(olderCommit)
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
