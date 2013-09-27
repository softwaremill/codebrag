package com.softwaremill.codebrag.dao.finders.commit

import com.softwaremill.codebrag.dao._
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.builder.{UserAssembler, CommitInfoAssembler}
import com.softwaremill.codebrag.common.{LoadMoreCriteria}
import com.softwaremill.codebrag.domain.{User, CommitInfo, CommitReviewTask}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.LoadMoreCriteria.PagingDirection

class ReviewableCommitsListFinderSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers {

  val finder = new ReviewableCommitsListFinder

  val commitDao = new MongoCommitInfoDAO
  val reviewTaskDao = new MongoCommitReviewTaskDAO
  val userDao = new MongoUserDAO

  val reviewingUserId = ObjectIdTestUtils.oid(100)
  val paging = LoadMoreCriteria.fromBeginning(2)

  val commitAuthor = UserAssembler.randomUser.withAvatarUrl("http://avatar.com").withFullName("John Doe").get

  val commitOne = CommitInfoAssembler.randomCommit.withAuthorName(commitAuthor.name).get
  val commitTwo = CommitInfoAssembler.randomCommit.withAuthorName(commitAuthor.name).get
  val commitThree = CommitInfoAssembler.randomCommit.withAuthorName(commitAuthor.name).get



  it should "find no commits when user has no commits to review" taggedAs RequiresDb in {
    // given
    storeCommits(commitOne, commitTwo, commitThree)

    // when
    val commitsView = finder.findCommitsToReviewFor(reviewingUserId, paging)

    // then
    commitsView.commits should be('empty)
  }

  it should "find commits when user has commits to review" taggedAs RequiresDb in {
    // given
    storeCommits(commitOne, commitTwo, commitThree)
    storeReviewTasksFor(reviewingUserId, commitOne, commitThree)

    // when
    val commitsView = finder.findCommitsToReviewFor(reviewingUserId, paging)

    // then
    commitsView.commits.map(_.id) should be(List(commitOne.id.toString, commitThree.id.toString))
  }

  it should "indicate that there are no previous commits" taggedAs RequiresDb in {
    // given
    storeCommits(commitOne, commitTwo, commitThree)
    storeReviewTasksFor(reviewingUserId, commitOne, commitTwo, commitThree)

    // when
    val twoCommitsOnPage = LoadMoreCriteria.fromBeginning(2)
    val commitsView = finder.findCommitsToReviewFor(reviewingUserId, twoCommitsOnPage)

    // then
    commitsView.older should equal(0)
  }

  it should "return information about how many more commits there are to review" taggedAs RequiresDb in {
    // given
    storeCommits(commitOne, commitTwo, commitThree)
    storeReviewTasksFor(reviewingUserId, commitOne, commitTwo, commitThree)

    // when
    val oneCommitToReview = LoadMoreCriteria.fromBeginning(1)
    val commitsView = finder.findCommitsToReviewFor(reviewingUserId, oneCommitToReview)

    // then
    commitsView.newer should equal(2)
  }

  it should "return page of next commits to review" taggedAs RequiresDb in {
    // given
    storeCommits(commitOne, commitTwo, commitThree)
    storeReviewTasksFor(reviewingUserId, commitOne, commitTwo, commitThree)

    // when
    val nextAfterSecondCommit = LoadMoreCriteria(commitTwo.id, PagingDirection.Right, 2)
    val commitsView = finder.findCommitsToReviewFor(reviewingUserId, nextAfterSecondCommit)

    // then
    commitsView.commits.map(_.id) should be(List(commitThree.id.toString))
  }

  it should "return commits view containing user data" taggedAs RequiresDb in {
    // given
    storeUser(commitAuthor)
    storeCommits(commitOne)
    storeReviewTasksFor(reviewingUserId, commitOne)

    // when
    val commitsView = finder.findCommitsToReviewFor(reviewingUserId, paging)

    // then
    val commitView = commitsView.commits.head
    commitView.authorAvatarUrl should be(commitAuthor.avatarUrl)
    commitView.authorName should be(commitAuthor.name)
  }

  private def storeUser(user: User) = userDao.add(user)

  private def storeCommits(commits: CommitInfo*) = commits.foreach(commitDao.storeCommit)

  private def storeReviewTasksFor(userId: ObjectId, commits: CommitInfo*) = {
    commits.foreach( commit => reviewTaskDao.save(CommitReviewTask(commit.id, userId)))
  }
}
