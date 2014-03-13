package com.softwaremill.codebrag.activities.finders

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.commitinfo.{SQLCommitInfoDAO, MongoCommitInfoDAO, CommitInfoDAO}
import com.softwaremill.codebrag.dao.reviewtask.{SQLCommitReviewTaskDAO, MongoCommitReviewTaskDAO, CommitReviewTaskDAO}
import com.softwaremill.codebrag.dao.user.{SQLUserDAO, MongoUserDAO, UserDAO}
import com.softwaremill.codebrag.dao.ObjectIdTestUtils
import com.softwaremill.codebrag.common.paging.PagingCriteria
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.builder.{CommitInfoAssembler, UserAssembler}
import com.softwaremill.codebrag.common.paging.PagingCriteria.Direction
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.{CommitReviewTask, CommitInfo, User}
import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL, ClearMongoDataAfterTest, FlatSpecWithMongo}
import com.softwaremill.codebrag.common.ClockSpec

trait ToReviewCommitsFinderSpec extends FlatSpec with ShouldMatchers with MockitoSugar {

//  var finder: ReviewableCommitsListFinder = _
//
//  def commitDao: CommitInfoDAO
//  def reviewTaskDao: CommitReviewTaskDAO
//  def userDao: UserDAO
//
//  val reviewingUserId = ObjectIdTestUtils.oid(100)
//  val paging = PagingCriteria.fromBeginning[ObjectId](2)
//
//  val commitAuthor = UserAssembler.randomUser.withAvatarUrl("http://avatar.com").withFullName("John Doe").get
//
//  it should "find no commits when user has no commits to review" taggedAs RequiresDb in {
//    // given
//    storeCommits(3, commitAuthor)
//
//    initFinder(Set())
//
//    // when
//    val commitsView = finder.findCommitsToReviewFor(reviewingUserId, paging)
//
//    // then
//    commitsView.commits should be('empty)
//  }
//
//  it should "find commits when user has commits to review" taggedAs RequiresDb in {
//    // given
//    val List(commitOne, _, commitThree) = storeCommits(3, commitAuthor)
//    storeReviewTasksFor(reviewingUserId, commitOne, commitThree)
//
//    initFinder(Set(commitOne.id, commitThree.id))
//
//    // when
//    val commitsView = finder.findCommitsToReviewFor(reviewingUserId, paging)
//
//    // then
//    commitsView.commits.map(_.id) should be(List(commitOne.id.toString, commitThree.id.toString))
//  }
//
//  it should "indicate that there are no previous commits" taggedAs RequiresDb in {
//    // given
//    val List(commitOne, commitTwo, commitThree) = storeCommits(3, commitAuthor)
//    storeReviewTasksFor(reviewingUserId, commitOne, commitTwo, commitThree)
//
//    initFinder(Set(commitOne.id, commitTwo.id, commitThree.id))
//
//    // when
//    val twoCommitsOnPage = PagingCriteria.fromBeginning[ObjectId](2)
//    val commitsView = finder.findCommitsToReviewFor(reviewingUserId, twoCommitsOnPage)
//
//    // then
//    commitsView.older should equal(0)
//  }
//
//  it should "return information about how many more commits there are to review" taggedAs RequiresDb in {
//    // given
//    val List(commitOne, commitTwo, commitThree) = storeCommits(3, commitAuthor)
//    storeReviewTasksFor(reviewingUserId, commitOne, commitTwo, commitThree)
//
//    initFinder(Set(commitOne.id, commitTwo.id, commitThree.id))
//
//    // when
//    val oneCommitToReview = PagingCriteria.fromBeginning[ObjectId](1)
//    val commitsView = finder.findCommitsToReviewFor(reviewingUserId, oneCommitToReview)
//
//    // then
//    commitsView.newer should equal(2)
//  }
//
//  it should "return page of next commits to review" taggedAs RequiresDb in {
//    // given
//    val List(commitOne, commitTwo, commitThree) = storeCommits(3, commitAuthor)
//    storeReviewTasksFor(reviewingUserId, commitOne, commitTwo, commitThree)
//
//    initFinder(Set(commitOne.id, commitTwo.id, commitThree.id))
//
//    // when
//    val nextAfterSecondCommit = PagingCriteria(commitTwo.id, Direction.Right, 2)
//    val commitsView = finder.findCommitsToReviewFor(reviewingUserId, nextAfterSecondCommit)
//
//    // then
//    commitsView.commits.map(_.id) should be(List(commitThree.id.toString))
//  }
//
//  it should "return commits view containing user data" taggedAs RequiresDb in {
//    // given
//    storeUser(commitAuthor)
//    val List(commitOne) = storeCommits(1, commitAuthor)
//    storeReviewTasksFor(reviewingUserId, commitOne)
//
//    initFinder(Set(commitOne.id))
//
//    // when
//    val commitsView = finder.findCommitsToReviewFor(reviewingUserId, paging)
//
//    // then
//    val commitView = commitsView.commits.head
//    commitView.authorAvatarUrl should equal(commitAuthor.settings.avatarUrl)
//    commitView.authorName should be(commitAuthor.name)
//  }
//
//  def initFinder(commitPendingReviewForReviewingUserId: Set[ObjectId]) {
//    val mockCommitReviewTaskDAO = mock[CommitReviewTaskDAO]
//    when(mockCommitReviewTaskDAO.commitsPendingReviewFor(reviewingUserId)).thenReturn(commitPendingReviewForReviewingUserId)
//
//    finder = new ReviewableCommitsListFinder(mockCommitReviewTaskDAO, commitDao, userDao)
//  }
//
//  private def storeUser(user: User) = userDao.add(user)
//
//  private def storeCommits(count: Int, commitAuthor: User) = {
//    val storedCommits = for(i <- 1 to count) yield {
//      val commit = CommitInfoAssembler.randomCommit.withAuthorName(commitAuthor.name).get
//      commitDao.storeCommit(commit)
//    }
//    storedCommits.toList
//  }
//
//  private def storeReviewTasksFor(userId: ObjectId, commits: CommitInfo*) = {
//    commits.foreach( commit => reviewTaskDao.save(CommitReviewTask(commit.id, userId)))
//  }

}