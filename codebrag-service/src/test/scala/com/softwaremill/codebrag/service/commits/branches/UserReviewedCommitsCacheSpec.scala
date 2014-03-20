package com.softwaremill.codebrag.service.commits.branches

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.reviewedcommits.ReviewedCommitsDAO
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.common.ClockSpec
import org.mockito.Mockito
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.domain.ReviewedCommit
import org.mockito.Mockito._

class UserReviewedCommitsCacheSpec extends FlatSpec with MockitoSugar with BeforeAndAfter with ShouldMatchers with ClockSpec {

  var cache: UserReviewedCommitsCache = _
  var userDao: UserDAO = _
  var reviewedCommitsDao: ReviewedCommitsDAO = _

  val Now = clock.nowUtc
  val User = UserAssembler.randomUser.withToReviewStartDate(Now).get
  val UserReviewedCommits = Set.empty[ReviewedCommit]

  before {
    userDao = mock[UserDAO]
    reviewedCommitsDao = mock[ReviewedCommitsDAO]
    cache = new UserReviewedCommitsCache(userDao, reviewedCommitsDao)
  }

  it should "add new user entry to cache and persist it when user is registered" in {
    // given
    val newUserEntry = UserReviewedCommitsCacheEntry.forNewlyRegisteredUser(User.id, Now)

    // when
    cache.addNewUserEntry(newUserEntry)

    // then
    val cachedEntry = cache.getUserEntry(User.id)
    cachedEntry should be(newUserEntry)
    verify(userDao).setToReviewStartDate(User.id, Now)
  }

  it should "ask backend for data when user not available in cache" in {
    // given
    when(userDao.findById(User.id)).thenReturn(Some(User))
    when(reviewedCommitsDao.allReviewedByUser(User.id)).thenReturn(UserReviewedCommits)

    // when
    val userEntry = cache.getUserEntry(User.id)

    // then
    userEntry.commits should be(UserReviewedCommits)
    userEntry.toReviewStartDate should be(Now)
  }

  it should "get entry from cache when available" in {
    // given
    val newUserEntry = UserReviewedCommitsCacheEntry.forNewlyRegisteredUser(User.id, Now)
    cache.addNewUserEntry(newUserEntry)

    // when
    val cachedEntry = cache.getUserEntry(User.id)

    // then
    cachedEntry should be(newUserEntry)
    verify(userDao, Mockito.never()).findById(User.id)
    verifyZeroInteractions(reviewedCommitsDao)
  }

}
