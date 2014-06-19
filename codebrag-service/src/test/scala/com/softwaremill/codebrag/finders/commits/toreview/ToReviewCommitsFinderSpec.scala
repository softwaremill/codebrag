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

class ToReviewCommitsFinderSpec extends FlatSpec with ShouldMatchers with MockitoSugar with BeforeAndAfter with ClockSpec {

  var finder: ToReviewCommitsFinder = _

  var repositoriesCache: RepositoriesCache = _
  var userDao: UserDAO = _
  var toReviewFilter: ToReviewBranchCommitsFilter = _
  var toReviewViewBuilder: ToReviewCommitsViewBuilder = _

  val MasterBranch = "master"
  val FeatureBranch = "feature"
  val BugfixBranch = "bugfix"

  val CodebragRepo = "codebrag"
  val BootzookaRepo = "bootzooka"

  val Alice = UserAssembler.randomUser.get  // has no selected branch
  val AliceCacheEntry = UserReviewedCommitsCacheEntry(Alice.id, Set.empty, clock.now)

  val Bob = UserAssembler.randomUser.withSelectedBranch(FeatureBranch).get
  val BobCacheEntry = UserReviewedCommitsCacheEntry(Bob.id, Set.empty, clock.now)

  val Page = PagingCriteria.fromBeginning[String](10)
  val NoCommitsInBranch = List.empty[BranchCommitCacheEntry]

  before {
    repositoriesCache = mock[RepositoriesCache]
    userDao = mock[UserDAO]
    toReviewFilter = mock[ToReviewBranchCommitsFilter]
    toReviewViewBuilder = mock[ToReviewCommitsViewBuilder]

    finder = new ToReviewCommitsFinder(repositoriesCache, userDao, toReviewFilter, toReviewViewBuilder)

    when(userDao.findById(Alice.id)).thenReturn(Some(Alice))
    when(userDao.findById(Bob.id)).thenReturn(Some(Bob))
  }

  it should "use provided branch and repo to find commits" in {
    // given
    when(repositoriesCache.getBranchCommits(CodebragRepo, MasterBranch)).thenReturn(NoCommitsInBranch)
    when(toReviewFilter.filterCommitsToReview(NoCommitsInBranch, Bob)).thenReturn(List.empty)

    // when
    finder.find(Bob.id, Some(CodebragRepo), Some(MasterBranch), Page)
    finder.count(Bob.id, Some(CodebragRepo), Some(MasterBranch))

    // then
    verify(repositoriesCache, times(2)).getBranchCommits(CodebragRepo, MasterBranch)
  }

  it should "use user-saved branch if no branch provided and user has branch stored" in {
    // given
    when(repositoriesCache.getBranchCommits(CodebragRepo, FeatureBranch)).thenReturn(NoCommitsInBranch)
    when(toReviewFilter.filterCommitsToReview(NoCommitsInBranch, Bob)).thenReturn(List.empty)

    // when
    finder.find(Bob.id, Some(CodebragRepo), branchNameOpt = None, Page)
    finder.count(Bob.id, Some(CodebragRepo), branchName = None)

    // then
    verify(repositoriesCache, times(2)).getBranchCommits(CodebragRepo, FeatureBranch)
  }

  it should "use currently checked out branch for given repo if no branch provided and user has no branch stored" in {
    // given
    when(repositoriesCache.getCheckedOutBranchShortName(CodebragRepo)).thenReturn(BugfixBranch)
    when(repositoriesCache.getBranchCommits(CodebragRepo, BugfixBranch)).thenReturn(NoCommitsInBranch)
    when(toReviewFilter.filterCommitsToReview(NoCommitsInBranch, Alice)).thenReturn(List.empty)

    // when
    finder.find(Alice.id, Some(CodebragRepo), branchNameOpt = None, Page)
    finder.count(Alice.id, Some(CodebragRepo), branchName = None)

    // then
    verify(repositoriesCache, times(2)).getBranchCommits(CodebragRepo, BugfixBranch)
  }

  it should "use repo name provided or use first one from the list of available repos if not provided explicitly" in {
    // given
    when(repositoriesCache.getBranchCommits(CodebragRepo, MasterBranch)).thenReturn(NoCommitsInBranch)
    when(repositoriesCache.getBranchCommits(BootzookaRepo, MasterBranch)).thenReturn(NoCommitsInBranch)
    when(repositoriesCache.repoNames).thenReturn(List(BootzookaRepo, CodebragRepo))
    when(toReviewFilter.filterCommitsToReview(NoCommitsInBranch, Alice)).thenReturn(List.empty)

    // when
    finder.find(Alice.id, Some(CodebragRepo), branchNameOpt = Some(MasterBranch), Page)
    finder.find(Alice.id, repoNameOpt = None, branchNameOpt = Some(MasterBranch), Page)

    // then
    verify(repositoriesCache).getBranchCommits(CodebragRepo, MasterBranch)
    verify(repositoriesCache).getBranchCommits(BootzookaRepo, MasterBranch)
  }

}