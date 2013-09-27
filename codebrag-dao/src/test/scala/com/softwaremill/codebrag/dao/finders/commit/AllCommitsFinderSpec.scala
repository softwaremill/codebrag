package com.softwaremill.codebrag.dao.finders.commit

import com.softwaremill.codebrag.dao._
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.LoadMoreCriteria
import com.softwaremill.codebrag.domain.builder.{CommitInfoAssembler, UserAssembler}
import com.softwaremill.codebrag.domain.{CommitReviewTask, CommitInfo, User}
import org.bson.types.ObjectId
import LoadMoreCriteria.PagingDirection

class AllCommitsFinderSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers {

  val finder = new AllCommitsFinder

  val commitDao = new MongoCommitInfoDAO
  val reviewTaskDao = new MongoCommitReviewTaskDAO
  val userDao = new MongoUserDAO

  val reviewingUserId = ObjectIdTestUtils.oid(100)
  val threeFromStart = LoadMoreCriteria.fromBeginning(3)

  val commitAuthor = UserAssembler.randomUser.withAvatarUrl("http://avatar.com").withFullName("John Doe").get

  val commitOne = CommitInfoAssembler.randomCommit.withAuthorName(commitAuthor.name).get
  val commitTwo = CommitInfoAssembler.randomCommit.withAuthorName(commitAuthor.name).get
  val commitThree = CommitInfoAssembler.randomCommit.withAuthorName(commitAuthor.name).get

  val ThreeCommitIdsList = List(commitOne, commitTwo, commitThree).map(_.id.toString)

  it should "find all commits when user only subset of commits to review" taggedAs RequiresDb in {
    // given
    storeCommits(commitOne, commitTwo, commitThree)
    storeReviewTasksFor(reviewingUserId, commitOne)

    // when
    val allCommitsView = finder.findAllCommits(threeFromStart, reviewingUserId)

    // then
    allCommitsView.commits.map(_.id) should equal(ThreeCommitIdsList)
  }

  it should "return next page from all commits" taggedAs RequiresDb in {
    // given
    storeCommits(commitOne, commitTwo, commitThree)
    storeReviewTasksFor(reviewingUserId, commitOne)

    // when
    val nextTwoAfterFirst = LoadMoreCriteria(commitOne.id, PagingDirection.Right, 2)
    val commitsView = finder.findAllCommits(nextTwoAfterFirst, reviewingUserId)

    // then
    commitsView.commits.map(_.id) should equal(List(commitTwo, commitThree).map(_.id.toString))
  }

  it should "return previous page from all commits" taggedAs RequiresDb in {
    // given
    storeCommits(commitOne, commitTwo, commitThree)
    storeReviewTasksFor(reviewingUserId, commitOne)

    // when
    val previousTwoFromLast = LoadMoreCriteria(commitThree.id, PagingDirection.Left, 2)
    val commitsView = finder.findAllCommits(previousTwoFromLast, reviewingUserId)

    // then
    commitsView.commits.map(_.id) should equal(List(commitOne, commitTwo).map(_.id.toString))
  }

  it should "return commits view containing user data" taggedAs RequiresDb in {
    // given
    storeUser(commitAuthor)
    storeCommits(commitOne)

    // when
    val commitsView = finder.findAllCommits(threeFromStart, reviewingUserId)

    // then
    val commitView = commitsView.commits.head
    commitView.authorAvatarUrl should equal(commitAuthor.avatarUrl)
    commitView.authorName should equal(commitAuthor.name)
  }

  it should "mark commits as pending review if not reviewed by user" taggedAs RequiresDb in {
    // given
    storeUser(commitAuthor)
    storeCommits(commitOne, commitTwo, commitThree)
    storeReviewTasksFor(reviewingUserId, commitTwo, commitThree)

    // when
    val commitsView = finder.findAllCommits(threeFromStart, reviewingUserId)

    // then
    val commits = commitsView.commits
    commits(0).pendingReview should equal(false)
    commits(1).pendingReview should equal(true)
    commits(2).pendingReview should equal(true)
  }

  it should "find commit with surroundings" taggedAs RequiresDb in {
    // given
    storeCommits(commitOne, commitTwo, commitThree)

    // when
    val twoInContext = LoadMoreCriteria(commitThree.id, PagingDirection.Radial, 2)
    val commitsView = finder.findAllCommits(twoInContext, reviewingUserId)

    // then
    commitsView.commits.map(_.id) should equal(ThreeCommitIdsList)
  }

  it should "find first commit with surroundings" taggedAs RequiresDb in {
    // given
    storeCommits(commitOne, commitTwo, commitThree)

    // when
    val oneInContext = LoadMoreCriteria(commitOne.id, PagingDirection.Radial, 1)
    val commitsView = finder.findAllCommits(oneInContext, reviewingUserId)

    // then
    commitsView.commits.map(_.id) should equal(List(commitOne, commitTwo).map(_.id.toString))
  }

  private def storeUser(user: User) = userDao.add(user)

  private def storeCommits(commits: CommitInfo*) = commits.foreach(commitDao.storeCommit)

  private def storeReviewTasksFor(userId: ObjectId, commits: CommitInfo*) = {
    commits.foreach(commit => reviewTaskDao.save(CommitReviewTask(commit.id, userId)))
  }
}
