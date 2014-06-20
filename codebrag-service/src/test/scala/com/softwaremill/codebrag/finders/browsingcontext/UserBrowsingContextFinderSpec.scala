package com.softwaremill.codebrag.finders.browsingcontext

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.browsingcontext.UserBrowsingContextDAO
import com.softwaremill.codebrag.cache.{RepositoryCache, RepositoriesCache}
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.domain.UserBrowsingContext

class UserBrowsingContextFinderSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfter {

  val contextDao = mock[UserBrowsingContextDAO]
  val repoCache = mock[RepositoriesCache]

  val finder = new UserBrowsingContextFinder(contextDao, repoCache)
  val Bob = UserAssembler.randomUser.get

  after {
    reset(contextDao, repoCache)
  }

  it should "find default context stored for user" in {
    // given
    val context = UserBrowsingContext(Bob.id, "codebrag", "bugfix")
    when(contextDao.findDefault(Bob.id)).thenReturn(Some(context))
    when(repoCache.hasRepo("codebrag")).thenReturn(true)

    // when
    val result = finder.findUserDefaultContext(Bob.id)

    // then
    result should be(context)
  }

  // shame on me, mocks returning mocks :(
  it should "fallback to system default context when default for user not found" in {
    // given
    when(contextDao.findDefault(Bob.id)).thenReturn(None)
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
    val userDefaultContext = UserBrowsingContext(Bob.id, "codebrag-nonexisting", "bugfix")
    when(contextDao.findDefault(Bob.id)).thenReturn(Some(userDefaultContext))
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