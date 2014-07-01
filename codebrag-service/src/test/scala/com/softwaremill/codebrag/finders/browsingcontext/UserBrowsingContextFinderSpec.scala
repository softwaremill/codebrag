package com.softwaremill.codebrag.finders.browsingcontext

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.repo.UserRepoDetailsDAO
import com.softwaremill.codebrag.cache.{RepositoryCache, RepositoriesCache}
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.domain.UserRepoDetails
import com.softwaremill.codebrag.common.ClockSpec

class UserBrowsingContextFinderSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfter with ClockSpec {

  val userRepoDetailsDao = mock[UserRepoDetailsDAO]
  val repoCache = mock[RepositoriesCache]

  val finder = new UserBrowsingContextFinder(userRepoDetailsDao, repoCache)
  val Bob = UserAssembler.randomUser.get

  after {
    reset(userRepoDetailsDao, repoCache)
  }

  it should "find default context stored for user" in {
    // given
    val repoDetails = UserRepoDetails(Bob.id, "codebrag", "bugfix", clock.nowUtc)
    when(userRepoDetailsDao.findDefault(Bob.id)).thenReturn(Some(repoDetails))
    when(repoCache.hasRepo("codebrag")).thenReturn(true)

    // when
    val result = finder.findUserDefaultContext(Bob.id)

    // then
    result should be(UserBrowsingContext(repoDetails))
  }

  // shame on me, mocks returning mocks :(
  it should "fallback to system default context when default for user not found" in {
    // given
    when(userRepoDetailsDao.findDefault(Bob.id)).thenReturn(None)
    when(repoCache.repoNames).thenReturn(List("codebrag"))
    val codebragRepoCache = mock[RepositoryCache]
    when(repoCache.getRepo("codebrag")).thenReturn(codebragRepoCache)
    when(codebragRepoCache.getCheckedOutBranchShortName).thenReturn("bugfix")

    // when
    val result = finder.findUserDefaultContext(Bob.id)

    // then
    val systemDefaultContext = UserBrowsingContext(Bob.id, "codebrag", "bugfix")
    result should be(systemDefaultContext)
  }

  // shame on me, mocks returning mocks :(
  it should "fallback to system default default context for user has nonexisting repository" in {
    // given
    val userDefaultRepoDetails = UserRepoDetails(Bob.id, "codebrag-nonexisting", "bugfix", clock.nowUtc)
    when(userRepoDetailsDao.findDefault(Bob.id)).thenReturn(Some(userDefaultRepoDetails))
    when(repoCache.repoNames).thenReturn(List("codebrag"))
    val codebragRepoCache = mock[RepositoryCache]
    when(repoCache.getRepo("codebrag")).thenReturn(codebragRepoCache)
    when(codebragRepoCache.getCheckedOutBranchShortName).thenReturn("bugfix")

    // when
    val result = finder.findUserDefaultContext(Bob.id)

    // then
    val systemDefaultContext = UserBrowsingContext(Bob.id, "codebrag", "bugfix")
    result should be(systemDefaultContext)
  }

}