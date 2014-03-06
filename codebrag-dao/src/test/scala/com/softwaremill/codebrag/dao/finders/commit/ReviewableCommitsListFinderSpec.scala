package com.softwaremill.codebrag.dao.finders.commit

import com.softwaremill.codebrag.dao._
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.builder.{UserAssembler, CommitInfoAssembler}
import com.softwaremill.codebrag.common.{ClockSpec, LoadMoreCriteria}
import com.softwaremill.codebrag.domain.{User, CommitInfo}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.LoadMoreCriteria.PagingDirection
import com.softwaremill.codebrag.dao.user.{SQLUserDAO, UserDAO, MongoUserDAO}
import com.softwaremill.codebrag.test.{FlatSpecWithSQL, ClearSQLDataAfterTest, FlatSpecWithMongo, ClearMongoDataAfterTest}
import com.softwaremill.codebrag.dao.commitinfo.{SQLCommitInfoDAO, CommitInfoDAO, MongoCommitInfoDAO}
import com.softwaremill.codebrag.dao.reviewtask.{SQLCommitReviewTaskDAO, CommitReviewTaskDAO, MongoCommitReviewTaskDAO}
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.CommitReviewTask
import org.scalatest.mock.MockitoSugar
import org.scalatest.FlatSpec

trait ReviewableCommitsListFinderSpec extends FlatSpec with ShouldMatchers with MockitoSugar {

  var finder: ReviewableCommitsListFinder = _

  def commitDao: CommitInfoDAO
  def reviewTaskDao: CommitReviewTaskDAO
  def userDao: UserDAO

  val reviewingUserId = ObjectIdTestUtils.oid(100)
  val paging = LoadMoreCriteria.fromBeginning(2)

  val commitAuthor = UserAssembler.randomUser.withAvatarUrl("http://avatar.com").withFullName("John Doe").get

  it should "find no commits when user has no commits to review" taggedAs RequiresDb in {
    // given
    storeCommits(3, commitAuthor)

    initFinder(Set())

    // when
    val commitsView = finder.findCommitsToReviewFor(reviewingUserId, paging)

    // then
    commitsView.commits should be('empty)
  }

  it should "find commits when user has commits to review" taggedAs RequiresDb in {
    // given
    val List(commitOne, _, commitThree) = storeCommits(3, commitAuthor)
    storeReviewTasksFor(reviewingUserId, commitOne, commitThree)

    initFinder(Set(commitOne.id, commitThree.id))

    // when
    val commitsView = finder.findCommitsToReviewFor(reviewingUserId, paging)

    // then
    commitsView.commits.map(_.id) should be(List(commitOne.id.toString, commitThree.id.toString))
  }

  it should "indicate that there are no previous commits" taggedAs RequiresDb in {
    // given
    val List(commitOne, commitTwo, commitThree) = storeCommits(3, commitAuthor)
    storeReviewTasksFor(reviewingUserId, commitOne, commitTwo, commitThree)

    initFinder(Set(commitOne.id, commitTwo.id, commitThree.id))

    // when
    val twoCommitsOnPage = LoadMoreCriteria.fromBeginning(2)
    val commitsView = finder.findCommitsToReviewFor(reviewingUserId, twoCommitsOnPage)

    // then
    commitsView.older should equal(0)
  }

  it should "return information about how many more commits there are to review" taggedAs RequiresDb in {
    // given
    val List(commitOne, commitTwo, commitThree) = storeCommits(3, commitAuthor)
    storeReviewTasksFor(reviewingUserId, commitOne, commitTwo, commitThree)

    initFinder(Set(commitOne.id, commitTwo.id, commitThree.id))

    // when
    val oneCommitToReview = LoadMoreCriteria.fromBeginning(1)
    val commitsView = finder.findCommitsToReviewFor(reviewingUserId, oneCommitToReview)

    // then
    commitsView.newer should equal(2)
  }

  it should "return page of next commits to review" taggedAs RequiresDb in {
    // given
    val List(commitOne, commitTwo, commitThree) = storeCommits(3, commitAuthor)
    storeReviewTasksFor(reviewingUserId, commitOne, commitTwo, commitThree)

    initFinder(Set(commitOne.id, commitTwo.id, commitThree.id))

    // when
    val nextAfterSecondCommit = LoadMoreCriteria(commitTwo.id, PagingDirection.Right, 2)
    val commitsView = finder.findCommitsToReviewFor(reviewingUserId, nextAfterSecondCommit)

    // then
    commitsView.commits.map(_.id) should be(List(commitThree.id.toString))
  }

  it should "return commits view containing user data" taggedAs RequiresDb in {
    // given
    storeUser(commitAuthor)
    val List(commitOne) = storeCommits(1, commitAuthor)
    storeReviewTasksFor(reviewingUserId, commitOne)

    initFinder(Set(commitOne.id))

    // when
    val commitsView = finder.findCommitsToReviewFor(reviewingUserId, paging)

    // then
    val commitView = commitsView.commits.head
    commitView.authorAvatarUrl should equal(commitAuthor.settings.avatarUrl)
    commitView.authorName should be(commitAuthor.name)
  }

  def initFinder(commitPendingReviewForReviewingUserId: Set[ObjectId]) {
    val mockCommitReviewTaskDAO = mock[CommitReviewTaskDAO]
    when(mockCommitReviewTaskDAO.commitsPendingReviewFor(reviewingUserId)).thenReturn(commitPendingReviewForReviewingUserId)

    finder = new ReviewableCommitsListFinder(mockCommitReviewTaskDAO, commitDao, userDao)
  }

  private def storeUser(user: User) = userDao.add(user)

  private def storeCommits(count: Int, commitAuthor: User) = {
    val storedCommits = for(i <- 1 to count) yield {
      val commit = CommitInfoAssembler.randomCommit.withAuthorName(commitAuthor.name).get
      commitDao.storeCommit(commit)
    }
    storedCommits.toList
  }

  private def storeReviewTasksFor(userId: ObjectId, commits: CommitInfo*) = {
    commits.foreach( commit => reviewTaskDao.save(CommitReviewTask(commit.id, userId)))
  }
}


class MongoReviewableCommitsListFinderSpec extends FlatSpecWithMongo with ClearMongoDataAfterTest with ReviewableCommitsListFinderSpec {
  val commitDao = new MongoCommitInfoDAO
  val reviewTaskDao = new MongoCommitReviewTaskDAO
  val userDao = new MongoUserDAO
}

class SQLReviewableCommitsListFinderSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with ReviewableCommitsListFinderSpec with ClockSpec {
  val commitDao = new SQLCommitInfoDAO(sqlDatabase)
  val reviewTaskDao = new SQLCommitReviewTaskDAO(sqlDatabase, clock)
  val userDao = new SQLUserDAO(sqlDatabase)
}