package com.softwaremill.codebrag.cache

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.reviewedcommits.ReviewedCommitsDAO
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.common.ClockSpec
import org.mockito.Mockito
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.domain.{UserRepoDetails, ReviewedCommit}
import org.mockito.Mockito._
import com.softwaremill.codebrag.dao.repo.UserRepoDetailsDAO

class UserReviewedCommitsCacheSpec extends FlatSpec with MockitoSugar with BeforeAndAfter with ShouldMatchers with ClockSpec {

  var cache: UserReviewedCommitsCache = _
  var userDao: UserDAO = _
  var reviewedCommitsDao: ReviewedCommitsDAO = _
  var userRepoDetailsDao: UserRepoDetailsDAO = _

  val Now = clock.nowUtc
  val User = UserAssembler.randomUser.withToReviewStartDate(Now).get
  val UserReviewedCommits = Set.empty[ReviewedCommit]

  before {
    userDao = mock[UserDAO]
    userRepoDetailsDao = mock[UserRepoDetailsDAO]
    reviewedCommitsDao = mock[ReviewedCommitsDAO]
    cache = new UserReviewedCommitsCache(userDao, reviewedCommitsDao, userRepoDetailsDao)
  }

  it should "ask backend for data when user not available in cache" in {
    // given
    val repoName = "codebrag"
    when(userDao.findById(User.id)).thenReturn(Some(User))
    when(reviewedCommitsDao.allReviewedByUser(User.id, repoName)).thenReturn(UserReviewedCommits)
    when(userRepoDetailsDao.find(User.id, repoName)).thenReturn(Some(UserRepoDetails(User.id, repoName, "master", Now)))

    // when
    val userEntry = cache.getEntry(User.id, repoName)

    // then
    userEntry.commits should be(UserReviewedCommits)
    userEntry.toReviewStartDate should be(Now)
  }

  it should "get entry from cache when available" in {
    // given
    val repoName = "codebrag"
    val userRepoDetails = UserRepoDetails(User.id, repoName, "master", clock.nowUtc)
    cache.initializeEmptyCacheFor(userRepoDetails)

    // when
    val cachedEntry = cache.getEntry(User.id, repoName)

    // then
    cachedEntry.commits should be('empty)
    cachedEntry.repoName should be(repoName)
    verify(userDao, Mockito.never()).findById(User.id)
    verifyZeroInteractions(reviewedCommitsDao)
  }

}
