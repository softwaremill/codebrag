package com.softwaremill.codebrag.finders

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.cache.{UserReviewedCommitsCacheEntry, UserReviewedCommitsCache}
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.service.config.ReviewProcessConfig
import org.mockito.Mockito._
import org.joda.time.DateTime
import com.softwaremill.codebrag.dao.finders.views.{CommitState, CommitView}
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.common.ClockSpec
import com.softwaremill.codebrag.finders.commits.CommitReviewStateAppender

class CommitReviewStateAppenderSpec extends FlatSpec with ShouldMatchers with MockitoSugar with BeforeAndAfter with ClockSpec {

  var reviewedCommitsCache: UserReviewedCommitsCache = _
  var userDao: UserDAO = _
  var reviewStateAppender: CommitReviewStateAppender = _ 

  val Alice = UserAssembler.randomUser.withFullName("Alice").withEmail("alice@email.com").get
  val Bob = UserAssembler.randomUser.withFullName("Bob").withEmail("bob@email.com").get
  val John = UserAssembler.randomUser.withFullName("John").withEmail("john@email.com").get

  val CodebragRepo = "codebrag"

  val BobCommit = buildCommitView("123", clock.now.minusDays(1), Bob)
  val BobCacheEntry = UserReviewedCommitsCacheEntry(Bob.id, CodebragRepo, Set.empty, clock.now.minusDays(2))
  val AliceCacheEntry = UserReviewedCommitsCacheEntry(Alice.id, CodebragRepo, Set.empty, clock.now.minusDays(2))
  val JohnCacheEntry = UserReviewedCommitsCacheEntry(John.id, CodebragRepo, Set.empty, clock.now)

  before {
    reviewedCommitsCache = mock[UserReviewedCommitsCache]
    userDao = mock[UserDAO]

    when(reviewedCommitsCache.getEntry(Bob.id, CodebragRepo)).thenReturn(BobCacheEntry)
    when(reviewedCommitsCache.getEntry(Alice.id, CodebragRepo)).thenReturn(AliceCacheEntry)
    when(reviewedCommitsCache.getEntry(John.id, CodebragRepo)).thenReturn(JohnCacheEntry)

    when(userDao.findById(Bob.id)).thenReturn(Some(Bob))
    when(userDao.findById(Alice.id)).thenReturn(Some(Alice))
    when(userDao.findById(John.id)).thenReturn(Some(John))
  }

  it should "set state to n/a when Bob's commit is too old to review for John" in {
    // given
    reviewStateAppender = appenderWith(reviewersCount = 1)

    // when
    val result = reviewStateAppender.setCommitReviewState(BobCommit, John.id)

    // then
    result.state should be(CommitState.NotApplicable)
  }

  it should "set state to ReviewedByMe for Alice when she reviewed Bob's commit and it is fully reviewed" in {
    // given
    markUsersReviewedCommit(BobCommit, Alice)
    reviewStateAppender = appenderWith(reviewersCount = 1)

    // when
    val result = reviewStateAppender.setCommitReviewState(BobCommit, Alice.id)

    // then
    result.state should be(CommitState.ReviewedByUser)
  }

  it should "set state to ReviewedByMe for Alice when she reviewed Bob's commit and it is not yet fully reviewed" in {
    // given
    markUsersReviewedCommit(BobCommit, Alice)
    reviewStateAppender = appenderWith(reviewersCount = 2)

    // when
    val result = reviewStateAppender.setCommitReviewState(BobCommit, Alice.id)

    // then
    result.state should be(CommitState.ReviewedByUser)
  }

  it should "set state to AwaitingOthersReview for Bob and his commit which is not yet fully reviewed" in {
    // given
    markUsersReviewedCommit(BobCommit)
    reviewStateAppender = appenderWith(reviewersCount = 1)

    // when
    val result = reviewStateAppender.setCommitReviewState(BobCommit, Bob.id)

    // then
    result.state should be(CommitState.AwaitingOthersReview)
  }

  it should "set state to FullyReviewedByOters for Bob and his commit which is fully reviewed" in {
    // given
    markUsersReviewedCommit(BobCommit, Alice)
    reviewStateAppender = appenderWith(reviewersCount = 1)

    // when
    val result = reviewStateAppender.setCommitReviewState(BobCommit, Bob.id)

    // then
    result.state should be(CommitState.ReviewedByOthers)
  }

  it should "set state to FullyReviewedByOters for Alice and Bob's commit when she didn't reviewed it but it is fully reviewed" in {
    // given
    markUsersReviewedCommit(BobCommit, John)
    reviewStateAppender = appenderWith(reviewersCount = 1)

    // when
    val result = reviewStateAppender.setCommitReviewState(BobCommit, Alice.id)

    // then
    result.state should be(CommitState.ReviewedByOthers)
  }

  it should "set state to AwaitingMyReview for Alice and Bob's commit when she didn't reviewed it and it is not yet fully reviewed" in {
    // given
    markUsersReviewedCommit(BobCommit, John)
    reviewStateAppender = appenderWith(reviewersCount = 2)

    // when
    val result = reviewStateAppender.setCommitReviewState(BobCommit, Alice.id)

    // then
    result.state should be(CommitState.AwaitingUserReview)
  }

  private def markUsersReviewedCommit(commit: CommitView, users: User*) {
    users.foreach { user =>
      when(reviewedCommitsCache.reviewedByUser(commit.sha, commit.repoName, user.id)).thenReturn(true)
    }
    when(reviewedCommitsCache.usersWhoReviewed(CodebragRepo, commit.sha)).thenReturn(users.map(_.id).toSet)
  }

  private def buildCommitView(sha: String, date: DateTime, author: User) = {
    val NA = ""
    CommitView(NA, CodebragRepo, sha, NA, author.name, author.emailLowerCase, date)
  }
  
  private def appenderWith(reviewersCount: Int) = {
    new TestCommitReviewStateAppender(userDao, reviewedCommitsCache, new TestReviewProcessConfig(reviewersCount))
  }

  class TestReviewProcessConfig(val reviewers: Int) extends ReviewProcessConfig {
    override def rootConfig = ???
    override lazy val requiredReviewersCount = reviewers
  }
  
  class TestCommitReviewStateAppender(val userDao: UserDAO, val reviewedCommitsCache: UserReviewedCommitsCache, val config: ReviewProcessConfig) extends CommitReviewStateAppender

}
