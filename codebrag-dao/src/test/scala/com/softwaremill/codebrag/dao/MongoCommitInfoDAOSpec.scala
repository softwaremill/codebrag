package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{CommitComment, CommitInfo}
import org.scalatest.{BeforeAndAfterEach, GivenWhenThen}
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.CommitInfoBuilder._
import org.joda.time.DateTime
import org.bson.types.ObjectId

class MongoCommitInfoDAOSpec extends FlatSpecWithMongo with GivenWhenThen with BeforeAndAfterEach with ShouldMatchers {
  val sampleCommit = createRandomCommit()
  var commitInfoDAO: MongoCommitInfoDAO = _
  val FixtureCommentId1 = new ObjectId("507f191e810c19729de860ea")
  val FixtureCommentId2 = new ObjectId("507f191e810c19729de860eb")

  val FixtureComments = List(CommitComment(FixtureCommentId1, "sofokles", "nice one", new DateTime),
                              CommitComment(FixtureCommentId2, "robert", "I like your style", new DateTime));

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
    val commit = createRandomCommit()

    When("trying to store it")
    commitInfoDAO.storeCommit(commit)

    Then("it is stored")
    commitInfoDAO.findBySha(commit.sha) should be('defined)
  }

  it should "store a collection of commits" in {
    Given("a collection of commits")
    val commit1 = createRandomCommit()
    val commit2 = createRandomCommit()
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
    val newerCommit = CommitInfo("123123123", "this is newer commit", "mostr", "mostr", new DateTime(), List.empty, List.empty)
    val anotherNewerCommit = CommitInfo("123123123", "this is another newer commit", "mostr", "mostr", new DateTime(), List.empty, FixtureComments)
    commitInfoDAO.storeCommit(newerCommit)
    commitInfoDAO.storeCommit(anotherNewerCommit)

    When("trying to find all stored commits")
    val commits = commitInfoDAO.findAll()

    Then("sample commit stored should be fetched")

    commits(0) should equal(newerCommit)
    commits(1) should equal(anotherNewerCommit)
    commits(2) should equal(olderCommit)
  }

}
