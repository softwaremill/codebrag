package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.CommitInfo
import org.scalatest.{BeforeAndAfterEach, GivenWhenThen}
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.CommitInfoBuilder._
import org.joda.time.DateTime
import org.bson.types.ObjectId

class MongoCommitInfoDAOSpec extends FlatSpecWithMongo with GivenWhenThen with BeforeAndAfterEach with ShouldMatchers {
  val sampleCommit = createRandomCommit(0)
  var commitInfoDAO: MongoCommitInfoDAO = _
  val EmptyListOfParents = List.empty
  val EmptyListOfFiles = List.empty

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

  it should "find all commits starting from newest" in {
    Given("a sample commit and another one stored")
    val olderCommit = sampleCommit
    val newerCommit = CommitInfo(commitId(1), "123123123", "this is newer commit", "mostr", "mostr", nowPlusSeconds(100), EmptyListOfParents, EmptyListOfFiles)
    val anotherNewerCommit = CommitInfo(commitId(2), "123123123", "this is newer commit", "mostr", "mostr", nowPlusSeconds(50), EmptyListOfParents, EmptyListOfFiles)
    commitInfoDAO.storeCommit(newerCommit)
    commitInfoDAO.storeCommit(anotherNewerCommit)

    When("trying to find all stored commits")
    val commits = commitInfoDAO.findAll()

    Then("sample commit stored should be fetched")

    commits(0) should equal(newerCommit)
    commits(1) should equal(anotherNewerCommit)
    commits(2) should equal(olderCommit)
  }

  private def commitId(number: Long): ObjectId = new ObjectId("507f191e810c19729de860e" + number)
  private def nowPlusSeconds(offset: Int) = new DateTime().plusSeconds(offset)

}
