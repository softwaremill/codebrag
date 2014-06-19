package com.softwaremill.codebrag.finders.commits.toreview

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.cache.{RepositoriesCache, BranchCommitCacheEntry, UserReviewedCommitsCacheEntry}
import com.softwaremill.codebrag.common.paging.PagingCriteria
import org.mockito.Mockito._
import com.softwaremill.codebrag.common.ClockSpec
import com.softwaremill.codebrag.domain.UserBrowsingContext
import com.softwaremill.codebrag.finders.browsingcontext.UserBrowsingContextFinder

class ToReviewCommitsFinderSpec extends FlatSpec with ShouldMatchers with MockitoSugar with BeforeAndAfter with ClockSpec {

  var finder: ToReviewCommitsFinder = _

  var repositoriesCache: RepositoriesCache = _
  var userDao: UserDAO = _
  var browsingContextFinder: UserBrowsingContextFinder = _
  var toReviewFilter: ToReviewBranchCommitsFilter = _
  var toReviewViewBuilder: ToReviewCommitsViewBuilder = _

  val MasterBranch = "master"
  val CodebragRepo = "codebrag"

  val Bob = UserAssembler.randomUser.get
  val BobCacheEntry = UserReviewedCommitsCacheEntry(Bob.id, Set.empty, clock.now)

  val Page = PagingCriteria.fromBeginning[String](10)
  val NoCommitsInBranch = List.empty[BranchCommitCacheEntry]

  before {
    repositoriesCache = mock[RepositoriesCache]
    userDao = mock[UserDAO]
    browsingContextFinder = mock[UserBrowsingContextFinder]
    toReviewFilter = mock[ToReviewBranchCommitsFilter]
    toReviewViewBuilder = mock[ToReviewCommitsViewBuilder]

    finder = new ToReviewCommitsFinder(repositoriesCache, userDao, browsingContextFinder, toReviewFilter, toReviewViewBuilder)

    when(userDao.findById(Bob.id)).thenReturn(Some(Bob))
  }

  it should "use provided branch and repo to find commits" in {
    // given
    when(repositoriesCache.getBranchCommits(CodebragRepo, MasterBranch)).thenReturn(NoCommitsInBranch)
    when(toReviewFilter.filterCommitsToReview(NoCommitsInBranch, Bob)).thenReturn(List.empty)

    // when
    val context = UserBrowsingContext(Bob.id, CodebragRepo, MasterBranch)
    finder.find(context, Page)
    finder.count(context)

    // then
    verify(repositoriesCache, times(2)).getBranchCommits(CodebragRepo, MasterBranch)
  }

  it should "count commits to review for user default browsing context" in {
    // given
    val defaultContext = UserBrowsingContext(Bob.id, CodebragRepo, MasterBranch)
    when(browsingContextFinder.findUserDefaultContext(Bob.id)).thenReturn(defaultContext)
    when(repositoriesCache.getBranchCommits(CodebragRepo, MasterBranch)).thenReturn(NoCommitsInBranch)
    when(toReviewFilter.filterCommitsToReview(NoCommitsInBranch, Bob)).thenReturn(List.empty)

    // when
    finder.countForUserRepoAndBranch(Bob.id)

    // then
    verify(repositoriesCache).getBranchCommits(CodebragRepo, MasterBranch)
  }

}