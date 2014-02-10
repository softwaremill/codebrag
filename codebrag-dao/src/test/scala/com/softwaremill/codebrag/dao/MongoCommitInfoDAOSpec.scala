package com.softwaremill.codebrag.dao

import org.scalatest.matchers.ShouldMatchers
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.builder.{UserAssembler, CommitInfoAssembler}
import CommitInfoAssembler._
import com.softwaremill.codebrag.test.mongo.ClearMongoDataAfterTest
import com.softwaremill.codebrag.domain.User

class MongoCommitInfoDAOSpec extends FlatSpecWithMongo with ClearMongoDataAfterTest with ShouldMatchers {
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

  it should "find last commits (ordered) for user" taggedAs(RequiresDb) in {
    // given
    val tenDaysAgo = DateTime.now.minusDays(10)
    val John = UserAssembler.randomUser.withEmail("john@codebrag.com").get
    val Bob = UserAssembler.randomUser.withFullName("Bob Smith").get

    val commits = List(
      buildCommitWithMatchingUserEmail(user = John, date = tenDaysAgo.plusDays(1), sha = "1"),
      buildCommitWithMatchingUserEmail(user = Bob, date = tenDaysAgo.plusDays(2), sha = "2"),
      buildCommitWithMatchingUserEmail(user = John, date = tenDaysAgo.plusDays(3), sha = "3"),
      buildCommitWithMatchingUserEmail(user = Bob, date = tenDaysAgo.plusDays(4), sha = "4"),
      buildCommitWithMatchingUserEmail(user = Bob, date = tenDaysAgo.plusDays(5), sha = "5"),
      buildCommitWithMatchingUserEmail(user = John, date = tenDaysAgo.plusDays(6), sha = "6"),
      buildCommitWithMatchingUserEmail(user = John, date = tenDaysAgo.plusDays(7), sha = "7")
    )
    commits.foreach(commitInfoDAO.storeCommit)
    
    // when
    val threeCommitsNotByJohn = commitInfoDAO.findLastCommitsNotAuthoredByUser(John, 3)
    val atMostTenCommitsNotByBob = commitInfoDAO.findLastCommitsNotAuthoredByUser(Bob, 10)

    // then
    threeCommitsNotByJohn.map(_.sha) should be(List("5", "4", "2"))
    atMostTenCommitsNotByBob.map(_.sha) should be(List("7", "6", "3", "1"))
  }

  it should "find last commit authored by user" in {
    // given
    val tenDaysAgo = DateTime.now.minusDays(10)
    val John = UserAssembler.randomUser.withFullName("John Doe").get
    val Bob = UserAssembler.randomUser.withFullName("Bob Smith").get
    val Alice = UserAssembler.randomUser.withFullName("Alice Smith").get

    val commits = List(
      buildCommitWithMatchingUserName(John, date = tenDaysAgo.plusDays(1), sha = "1"),
      buildCommitWithMatchingUserName(Bob, date = tenDaysAgo.plusDays(2), sha = "2"),
      buildCommitWithMatchingUserName(Bob, date = tenDaysAgo.plusDays(4), sha = "4"),
      buildCommitWithMatchingUserName(John, date = tenDaysAgo.plusDays(6), sha = "6")
    )
    commits.foreach(commitInfoDAO.storeCommit)

    // when
    val lastCommitByBob = commitInfoDAO.findLastCommitsAuthoredByUser(Bob, 1)
    val lastCommitsByJohn = commitInfoDAO.findLastCommitsAuthoredByUser(John, 2)
    val noCommitByAlice = commitInfoDAO.findLastCommitsAuthoredByUser(Alice, 1)

    // then
    lastCommitByBob.map(_.sha) should be(List("4"))
    lastCommitsByJohn.map(_.sha) should be(List("6", "1"))
    noCommitByAlice should be('empty)
  }

  it should "find user commits authored since given date" in {
    // given
    val hourAgo = DateTime.now.minusDays(10)
    val John = UserAssembler.randomUser.withFullName("John Doe").get
    val Bob = UserAssembler.randomUser.withFullName("Bob Smith").get

    val commits = List(
      buildCommitWithMatchingUserName(John, date = hourAgo.plusMinutes(5), sha = "1"),
      buildCommitWithMatchingUserName(Bob, date = hourAgo.plusMinutes(10), sha = "2"),
      buildCommitWithMatchingUserName(Bob, date = hourAgo.plusMinutes(15), sha = "4"),
      buildCommitWithMatchingUserName(John, date = hourAgo.plusMinutes(20), sha = "6")
    )
    commits.foreach(commitInfoDAO.storeCommit)

    // when
    val commitsByBobSince8mins = commitInfoDAO.findLastCommitsAuthoredByUserSince(Bob, hourAgo.plusMinutes(8))
    val commitsByBobSince10mins = commitInfoDAO.findLastCommitsAuthoredByUserSince(Bob, hourAgo.plusMinutes(15))
    val commitsByBobSince16mins = commitInfoDAO.findLastCommitsAuthoredByUserSince(Bob, hourAgo.plusMinutes(16))
    val commitsByBobSince15minsAnd1Sec = commitInfoDAO.findLastCommitsAuthoredByUserSince(Bob, hourAgo.plusMinutes(15).plusSeconds(1))

    // then
    commitsByBobSince8mins.map(_.sha) should be(List("2", "4"))
    commitsByBobSince10mins.map(_.sha) should be(List("4"))
    commitsByBobSince16mins.map(_.sha) should be('empty)
    commitsByBobSince15minsAnd1Sec.map(_.sha) should be('empty)
  }

  def buildCommitWithMatchingUserEmail(user: User, date: DateTime, sha: String) = {
    CommitInfoAssembler.randomCommit.withAuthorEmail(user.emailLowerCase).withAuthorDate(date).withSha(sha).get
  }

  def buildCommitWithMatchingUserName(user: User, date: DateTime, sha: String) = {
    CommitInfoAssembler.randomCommit.withAuthorName(user.name).withAuthorDate(date).withSha(sha).get
  }

}
