package com.softwaremill.codebrag.activities.finders

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.commitinfo.{SQLCommitInfoDAO, MongoCommitInfoDAO}
import com.softwaremill.codebrag.dao.reviewtask.{SQLCommitReviewTaskDAO, MongoCommitReviewTaskDAO}
import com.softwaremill.codebrag.dao.user.{SQLUserDAO, MongoUserDAO}
import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL, ClearMongoDataAfterTest, FlatSpecWithMongo}
import com.softwaremill.codebrag.common.ClockSpec

trait AllCommitsFinderSpec extends FlatSpec with ShouldMatchers with MockitoSugar {
//  var finder: AllCommitsFinder = _
//
//  def commitDao: CommitInfoDAO
//  def reviewTaskDao: CommitReviewTaskDAO
//  def userDao: UserDAO
//
//  val reviewingUserId = ObjectIdTestUtils.oid(100)
//  val threeFromStart = PagingCriteria.fromBeginning[ObjectId](3)
//
//  val commitAuthor = UserAssembler.randomUser.withAvatarUrl("http://avatar.com").withFullName("John Doe").get
//
//  it should "find all commits when user only subset of commits to review" taggedAs RequiresDb in {
//    // given
//    val List(commitOne, commitTwo, commitThree) = storeCommits(3, commitAuthor)
//    storeReviewTasksFor(reviewingUserId, commitOne)
//
//    initFinder(Set(commitOne.id))
//
//    // when
//    val allCommitsView = finder.findAllCommits(threeFromStart, reviewingUserId)
//
//    // then
//    val allIds = List(commitOne, commitTwo, commitThree).map(_.id.toString)
//    allCommitsView.commits.map(_.id) should be(allIds)
//  }
//
//  it should "return next page from all commits" taggedAs RequiresDb in {
//    // given
//    val List(commitOne, commitTwo, commitThree) = storeCommits(3, commitAuthor)
//    storeReviewTasksFor(reviewingUserId, commitOne)
//
//    initFinder(Set(commitOne.id))
//
//    // when
//    val nextTwoAfterFirst = PagingCriteria(commitOne.id, Direction.Right, 2)
//    val commitsView = finder.findAllCommits(nextTwoAfterFirst, reviewingUserId)
//
//    // then
//    val nextIds = List(commitTwo, commitThree).map(_.id.toString)
//    commitsView.commits.map(_.id) should equal(nextIds)
//  }
//
//  it should "return previous page from all commits" taggedAs RequiresDb in {
//    // given
//    val List(commitOne, commitTwo, commitThree) = storeCommits(3, commitAuthor)
//    storeReviewTasksFor(reviewingUserId, commitOne)
//
//    initFinder(Set(commitOne.id))
//
//    // when
//    val previousTwoFromLast = PagingCriteria(commitThree.id, Direction.Left, 2)
//    val commitsView = finder.findAllCommits(previousTwoFromLast, reviewingUserId)
//
//    // then
//    commitsView.commits.map(_.id) should equal(List(commitOne, commitTwo).map(_.id.toString))
//  }
//
//  it should "return commits view containing user data" taggedAs RequiresDb in {
//    // given
//    storeUser(commitAuthor)
//    storeCommits(1, commitAuthor)
//
//    initFinder(Set())
//
//    // when
//    val commitsView = finder.findAllCommits(threeFromStart, reviewingUserId)
//
//    // then
//    val commitView = commitsView.commits.head
//    commitView.authorAvatarUrl should equal(commitAuthor.settings.avatarUrl)
//    commitView.authorName should equal(commitAuthor.name)
//  }
//
//  it should "mark commits as pending review if not reviewed by user" taggedAs RequiresDb in {
//    // given
//    storeUser(commitAuthor)
//    val List(_, commitTwo, commitThree) = storeCommits(3, commitAuthor)
//    storeReviewTasksFor(reviewingUserId, commitTwo, commitThree)
//
//    initFinder(Set(commitTwo.id, commitThree.id))
//
//    // when
//    val commitsView = finder.findAllCommits(threeFromStart, reviewingUserId)
//
//    // then
//    val commits = commitsView.commits
//    commits(0).pendingReview should equal(false)
//    commits(1).pendingReview should equal(true)
//    commits(2).pendingReview should equal(true)
//  }
//
//  it should "find commit with surroundings" taggedAs RequiresDb in {
//    // given
//    val List(commitOne, commitTwo, commitThree) = storeCommits(3, commitAuthor)
//
//    initFinder(Set())
//
//    // when
//    val twoInContext = PagingCriteria(commitThree.id, Direction.Radial, 2)
//    val commitsView = finder.findAllCommits(twoInContext, reviewingUserId)
//
//    // then
//    val allIds = List(commitOne, commitTwo, commitThree).map(_.id.toString)
//    commitsView.commits.map(_.id) should equal(allIds)
//  }
//
//  it should "find first commit with surroundings" taggedAs RequiresDb in {
//    // given
//    val List(commitOne, commitTwo, _) = storeCommits(3, commitAuthor)
//
//    initFinder(Set())
//
//    // when
//    val oneInContext = PagingCriteria(commitOne.id, Direction.Radial, 1)
//    val commitsView = finder.findAllCommits(oneInContext, reviewingUserId)
//
//    // then
//    commitsView.commits.map(_.id) should equal(List(commitOne, commitTwo).map(_.id.toString))
//  }
//
//  def initFinder(commitPendingReviewForReviewingUserId: Set[ObjectId]) {
//    val mockCommitReviewTaskDAO = mock[CommitReviewTaskDAO]
//    when(mockCommitReviewTaskDAO.commitsPendingReviewFor(reviewingUserId)).thenReturn(commitPendingReviewForReviewingUserId)
//
//    finder = new AllCommitsFinder(mockCommitReviewTaskDAO, commitDao, userDao)
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
//    commits.foreach(commit => reviewTaskDao.save(CommitReviewTask(commit.id, userId)))
//  }
}