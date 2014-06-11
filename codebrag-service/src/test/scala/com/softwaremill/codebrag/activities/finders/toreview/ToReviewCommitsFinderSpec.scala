package com.softwaremill.codebrag.activities.finders.toreview

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.cache.{BranchCommitCacheEntry, UserReviewedCommitsCacheEntry, RepositoryCache}
import com.softwaremill.codebrag.common.paging.PagingCriteria
import org.mockito.Mockito._
import com.softwaremill.codebrag.common.ClockSpec

class ToReviewCommitsFinderSpec extends FlatSpec with ShouldMatchers with MockitoSugar with BeforeAndAfter with ClockSpec {

  var finder: ToReviewCommitsFinder = _

  var branchCommitsCache: RepositoryCache = _
  var userDao: UserDAO = _
  var toReviewFilter: ToReviewBranchCommitsFilter = _
  var toReviewViewBuilder: ToReviewCommitsViewBuilder = _

  val MasterBranch = "master"
  val FeatureBranch = "feature"
  val BugfixBranch = "bugfix"
  
  val Alice = UserAssembler.randomUser.get  // has no selected branch
  val AliceCacheEntry = UserReviewedCommitsCacheEntry(Alice.id, Set.empty, clock.now)

  val Bob = UserAssembler.randomUser.withSelectedBranch(FeatureBranch).get
  val BobCacheEntry = UserReviewedCommitsCacheEntry(Bob.id, Set.empty, clock.now)

  val Page = PagingCriteria.fromBeginning[String](10)
  val NoCommitsInBranch = List.empty[BranchCommitCacheEntry]

  before {
    branchCommitsCache = mock[RepositoryCache]
    userDao = mock[UserDAO]
    toReviewFilter = mock[ToReviewBranchCommitsFilter]
    toReviewViewBuilder = mock[ToReviewCommitsViewBuilder]

    finder = new ToReviewCommitsFinder(branchCommitsCache, userDao, toReviewFilter, toReviewViewBuilder)

    when(userDao.findById(Alice.id)).thenReturn(Some(Alice))
    when(userDao.findById(Bob.id)).thenReturn(Some(Bob))
  }

  it should "use provided branch to find commits in" in {
    // given
    when(branchCommitsCache.getBranchCommits(MasterBranch)).thenReturn(NoCommitsInBranch)
    when(toReviewFilter.filterFor(NoCommitsInBranch, Bob)).thenReturn(List.empty)

    // when
    finder.find(Bob.id, Some(MasterBranch), Page)
    finder.count(Bob.id, Some(MasterBranch))

    // then
    verify(branchCommitsCache, times(2)).getBranchCommits(MasterBranch)
  }

  it should "use user-saved branch if no branch provided and user has branch stored" in {
    // given
    when(branchCommitsCache.getBranchCommits(FeatureBranch)).thenReturn(NoCommitsInBranch)
    when(toReviewFilter.filterFor(NoCommitsInBranch, Bob)).thenReturn(List.empty)

    // when
    finder.find(Bob.id, providedBranchName = None, Page)
    finder.count(Bob.id, branchName = None)

    // then
    verify(branchCommitsCache, times(2)).getBranchCommits(FeatureBranch)
  }

  it should "use currently checked out branch if no branch provided and user has no branch stored" in {
    // given
    when(branchCommitsCache.getCheckedOutBranchShortName).thenReturn(BugfixBranch)
    when(branchCommitsCache.getBranchCommits(BugfixBranch)).thenReturn(NoCommitsInBranch)
    when(toReviewFilter.filterFor(NoCommitsInBranch, Alice)).thenReturn(List.empty)

    // when
    finder.find(Alice.id, providedBranchName = None, Page)
    finder.count(Alice.id, branchName = None)

    // then
    verify(branchCommitsCache, times(2)).getBranchCommits(BugfixBranch)
  }

}