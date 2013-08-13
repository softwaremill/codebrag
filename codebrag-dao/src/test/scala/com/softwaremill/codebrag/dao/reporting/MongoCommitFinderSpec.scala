package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao._
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{CommitReviewTask, Authentication, User, CommitInfo}
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import com.softwaremill.codebrag.dao.reporting.views.{CommitView, CommitListView}
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import com.softwaremill.codebrag.common.{PagingCriteria, SurroundingsCriteria}


class MongoCommitFinderSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers {

  val commitListFinder = new MongoCommitFinder
  var commitReviewTaskDao = new  MongoCommitReviewTaskDAO
  val commitInfoDao = new MongoCommitInfoDAO

  val userId = ObjectIdTestUtils.oid(123)
  val user = User(userId, Authentication.basic("user", "password"), "John Doe", "john@doe.com", "123", "avatarUrl")
  val DefaultFixturePaging = PagingCriteria(None, None, 5)

  it should "find page of commits to review for given user only" taggedAs(RequiresDb) in {
    // given
    val storedCommits = prepareAndStoreSomeCommits(howMany = 5)
    storeCommitReviewTasksFor(userId, storedCommits(0), storedCommits(1))

    // when
    val commitsFound = commitListFinder.findCommitsToReviewForUser(userId, DefaultFixturePaging)

    // then
    commitsFound.commits should have size(2)
  }

  it should "find a page of reviewable commits and count their total number" taggedAs(RequiresDb) in {
    // given
    val storedCommits = prepareAndStoreSomeCommits(howMany = 15)
    storeCommitReviewTasksFor(userId, storedCommits.take(14) : _*)

    // when
    val commitsFound = commitListFinder.findCommitsToReviewForUser(userId, DefaultFixturePaging)

    // then
    commitsFound.totalCount should equal(14)
  }

  it should "return next commits after given id" taggedAs(RequiresDb) in {
    // given
    val storedCommits = prepareAndStoreSomeCommits(howMany = 15)
    storeCommitReviewTasksFor(userId, storedCommits.take(14) : _*)

    // when
    val lastKnownId = storedCommits(3).id
    val commitsFound = commitListFinder.findCommitsToReviewForUser(userId, PagingCriteria(None, Some(lastKnownId), 2))

    // then
    commitsFound.commits.size should equal(2)
    commitsFound.commits(0).id should equal(storedCommits(4).id.toString)
    commitsFound.commits(1).id should equal(storedCommits(5).id.toString)
  }

  it should "return no items when id provided doesn't exist collection" taggedAs(RequiresDb) in {
    // given
    val storedCommits = prepareAndStoreSomeCommits(howMany = 15)
    storeCommitReviewTasksFor(userId, storedCommits.take(14) : _*)

    // when
    val nonExistingLastId = new ObjectId
    val commitsFound = commitListFinder.findCommitsToReviewForUser(userId, PagingCriteria(None, Some(nonExistingLastId), 2))

    // then
    commitsFound.commits should be('empty)
  }

  it should "return first items when commit id not provided on search" taggedAs(RequiresDb) in {
    // given
    val storedCommits = prepareAndStoreSomeCommits(howMany = 15)
    storeCommitReviewTasksFor(userId, storedCommits.take(14) : _*)

    // when
    val commitsFound = commitListFinder.findCommitsToReviewForUser(userId, PagingCriteria(None, None, 2))

    // then
    commitsFound.commits.map(_.id) should be(List(storedCommits(0).id.toString, storedCommits(1).id.toString))
  }

  it should "find reviewable commits starting from oldest commit date" taggedAs(RequiresDb) in {
    // given
    val baseDate = DateTime.now()
    val olderCommit = CommitInfoAssembler.randomCommit.withSha("111").
      withCommitDate(baseDate).
      withAuthorDate(baseDate.plusSeconds(11)).get
    val newerCommit = CommitInfoAssembler.randomCommit.withSha("222").
      withCommitDate(baseDate.plusSeconds(10)).
      withAuthorDate(baseDate.plusSeconds(10)).get
    commitInfoDao.storeCommit(newerCommit)
    commitInfoDao.storeCommit(olderCommit)
    storeCommitReviewTasksFor(userId, olderCommit, newerCommit)

    // when
    val pendingCommitList = commitListFinder.findCommitsToReviewForUser(userId, DefaultFixturePaging)

    //then
    pendingCommitList.commits.length should equal (2)
    pendingCommitList.commits(0).sha should equal(olderCommit.sha)
    pendingCommitList.commits(1).sha should equal(newerCommit.sha)
  }

  it should "sort pending commits with same commit date by author date" taggedAs(RequiresDb) in {
    // given
    val commitDate = DateTime.now()
    val commits = List(
    CommitInfoAssembler.randomCommit.withSha("111").withCommitDate(commitDate).
      withAuthorDate(commitDate.plusSeconds(11)).get,
    CommitInfoAssembler.randomCommit.withSha("222").withCommitDate(commitDate).
      withAuthorDate(commitDate.plusSeconds(50)).get,
    CommitInfoAssembler.randomCommit.withSha("333").withCommitDate(commitDate).
      withAuthorDate(commitDate.plusSeconds(5)).get)
    commits.foreach({commitInfoDao.storeCommit(_)})

    storeCommitReviewTasksFor(userId, commits : _*)

    // when
    val pendingCommitList = commitListFinder.findCommitsToReviewForUser(userId, PagingCriteria(None, None, 3))

    //then
    pendingCommitList.commits.length should equal (3)
    pendingCommitList.commits(0).sha should equal(commits(2).sha)
    pendingCommitList.commits(1).sha should equal(commits(0).sha)
    pendingCommitList.commits(2).sha should equal(commits(1).sha)
  }

  it should "sort commits with surroundings with the same commit date by author date" taggedAs(RequiresDb) in {
    // given
    val commitDate = DateTime.now()
    val commits = List(
      CommitInfoAssembler.randomCommit.withSha("111").withCommitDate(commitDate).
        withAuthorDate(commitDate.plusSeconds(11)).get,
      CommitInfoAssembler.randomCommit.withSha("222").withCommitDate(commitDate).
        withAuthorDate(commitDate.plusSeconds(50)).get,
      CommitInfoAssembler.randomCommit.withSha("333").withCommitDate(commitDate).
        withAuthorDate(commitDate.plusSeconds(5)).get)
    commits.foreach({commitInfoDao.storeCommit(_)})

    // when
    val centralCommitId = commits(1).id
    val pendingCommitList = commitListFinder.findSurroundings(SurroundingsCriteria(centralCommitId, 3), userId)

    //then

    pendingCommitList.commits.length should equal (3)
    pendingCommitList.commits(0).sha should equal(commits(2).sha)
    pendingCommitList.commits(1).sha should equal(commits(0).sha)
    pendingCommitList.commits(2).sha should equal(commits(1).sha)
  }

  it should "find empty list if there are no commits to review for user" taggedAs(RequiresDb) in {
    // given
    prepareAndStoreSomeCommits(5)

    // when
    val pendingCommitList = commitListFinder.findCommitsToReviewForUser(userId, DefaultFixturePaging)

    //then
    pendingCommitList.commits should be ('empty)
  }

  it should "find commit info (without files) by given id" taggedAs(RequiresDb) in {
    // given
    val commitId = ObjectIdTestUtils.oid(111)
    val commit = CommitInfoAssembler.randomCommit.withId(commitId).withSha("111").withMessage("Commit message").get
    commitInfoDao.storeCommit(commit)

    // when
    val Right(foundCommit) = commitListFinder.findCommitInfoById(commitId.toString, userId)

    //then
    foundCommit.message should equal(commit.message)
    foundCommit.sha should equal(commit.sha)
    foundCommit.pendingReview should be (false)
  }

  it should "mark commit view as pending review if task exists" taggedAs(RequiresDb) in {
    // given
    val commitId = ObjectIdTestUtils.oid(111)
    val commit = CommitInfoAssembler.randomCommit.withId(commitId).withSha("111").withMessage("Commit message").get
    commitInfoDao.storeCommit(commit)
    storeCommitReviewTasksFor(userId, commit)
    // when
    val Right(foundCommit) = commitListFinder.findCommitInfoById(commitId.toString, userId)

    //then
    foundCommit.pendingReview should be (true)
  }

  it should "result with error msg whem commit not found" taggedAs(RequiresDb) in {
    // given
    val nonExistingCommitId = ObjectIdTestUtils.oid(111)

    // when
    val Left(errorMsg) = commitListFinder.findCommitInfoById(nonExistingCommitId.toString, userId)

    //then
    errorMsg should be (s"No such commit $nonExistingCommitId")
  }

  it should "load commit together with its siblings" in {
    // given
    val baseDate = DateTime.now
    val commits = (1 to 10).map { i => CommitInfoAssembler.randomCommit.withCommitDate(baseDate.plusMinutes(i)).get}
    commits.foreach(commitInfoDao.storeCommit)

    // when
    val thirdCommit = commits(2)
    val foundCommits = commitListFinder.findSurroundings(SurroundingsCriteria(thirdCommit.id, 2), userId)

    // then
    foundCommits.commits.map(_.id) should be(commitsIdsAsStrings(commits(0), commits(1), commits(2), commits(3), commits(4)))
  }

  it should "load only next siblings when asked in context of first commit" in {
    // given
    val baseDate = DateTime.now
    val commits = (1 to 10).map { i => CommitInfoAssembler.randomCommit.withCommitDate(baseDate.plusMinutes(i)).get}
    commits.foreach(commitInfoDao.storeCommit)

    // when
    val foundCommits = commitListFinder.findSurroundings(SurroundingsCriteria(commits.head.id, 2), userId)

    // then
    foundCommits.commits.map(_.id) should be(commitsIdsAsStrings(commits(0), commits(1), commits(2)))
  }

  it should "load only next/previous siblings when asked in context of first/last commit" in {
    // given
    val baseDate = DateTime.now
    val commits = (1 to 10).map { i => CommitInfoAssembler.randomCommit.withCommitDate(baseDate.plusMinutes(i)).get}
    commits.foreach(commitInfoDao.storeCommit)

    // when
    val foundCommitsForFirst = commitListFinder.findSurroundings(SurroundingsCriteria(commits.head.id, 2), userId)
    val foundCommitsForLast = commitListFinder.findSurroundings(SurroundingsCriteria(commits.last.id, 2), userId)

    // then
    foundCommitsForFirst.commits.map(_.id) should be(commitsIdsAsStrings(commits(0), commits(1), commits(2)))
    foundCommitsForLast.commits.map(_.id) should be(commitsIdsAsStrings(commits(7), commits(8), commits(9)))
  }

  it should "return empty list when given commit not found" in {
    // given
    val baseDate = DateTime.now
    val commits = (1 to 10).map { i => CommitInfoAssembler.randomCommit.withCommitDate(baseDate.plusMinutes(i)).get}
    commits.foreach(commitInfoDao.storeCommit)

    // when
    val nonExistingCommitId = new ObjectId
    val result = commitListFinder.findSurroundings(SurroundingsCriteria(new ObjectId, 2), userId)

    // then
    result should be('empty)
  }

  def commitsIdsAsStrings(commits: CommitInfo*) = {
    commits.map(_.id.toString).toList
  }

  def prepareAndStoreSomeCommits(howMany: Int) = {
    val commitsPrepared = (1 to howMany).map{ i => CommitInfoAssembler.randomCommit.withSha(i.toString).withMessage(s"Commit message $i").get }
    commitsPrepared.foreach(commitInfoDao.storeCommit)
    commitsPrepared.toList
  }


  def storeCommitReviewTasksFor(userId: ObjectId, commits: CommitInfo*) {
    commits map { commit => CommitReviewTask(commit.id, user.id) } foreach { commitReviewTaskDao.save }
  }

  def foundCommitView(commitsFound: CommitListView, storedCommits: List[CommitInfo], index: Int): CommitView = {
    commitsFound.commits.find(commitView => commitView.id == storedCommits(index).id.toString).get
  }
}
