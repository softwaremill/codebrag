package com.softwaremill.codebrag.activities.finders

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.service.config.ReviewProcessConfig
import com.softwaremill.codebrag.cache.{BranchCommitCacheEntry, UserReviewedCommitsCacheEntry, UserReviewedCommitsCache, BranchCommitsCache}
import com.softwaremill.codebrag.common.paging.PagingCriteria
import org.mockito.Mockito._
import com.softwaremill.codebrag.common.ClockSpec
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.dao.finders.views.CommitListView

class ToReviewCommitsFinderSpec extends FlatSpec with ShouldMatchers with MockitoSugar with BeforeAndAfter with ClockSpec {

  var finder: ToReviewCommitsFinder = _

  var config: ReviewProcessConfig = _
  var branchCommitsCache: BranchCommitsCache = _
  var reviewedCommitsCache: UserReviewedCommitsCache = _
  var commitsInfoDao: CommitInfoDAO = _
  var userDao: UserDAO = _

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
    config = mock[ReviewProcessConfig]
    branchCommitsCache = mock[BranchCommitsCache]
    reviewedCommitsCache = mock[UserReviewedCommitsCache]
    commitsInfoDao = mock[CommitInfoDAO]
    userDao = mock[UserDAO]

    when(userDao.findById(Alice.id)).thenReturn(Some(Alice))
    when(reviewedCommitsCache.getUserEntry(Alice.id)).thenReturn(AliceCacheEntry)
    when(userDao.findById(Bob.id)).thenReturn(Some(Bob))
    when(reviewedCommitsCache.getUserEntry(Bob.id)).thenReturn(BobCacheEntry)
  }

  it should "use provided branch to find commits in" in {
    // given
    finder = finderWithEmptyResults
    when(branchCommitsCache.getBranchCommits(MasterBranch)).thenReturn(NoCommitsInBranch)

    // when
    finder.find(Bob.id, Some(MasterBranch), Page)
    finder.count(Bob.id, Some(MasterBranch))

    // then
    verify(branchCommitsCache, times(2)).getBranchCommits(MasterBranch)
  }

  it should "use user-saved branch if no branch provided and user has branch stored" in {
    // given
    finder = finderWithEmptyResults
    when(branchCommitsCache.getBranchCommits(FeatureBranch)).thenReturn(NoCommitsInBranch)

    // when
    finder.find(Bob.id, branchName = None, Page)
    finder.count(Bob.id, branchName = None)

    // then
    verify(branchCommitsCache, times(2)).getBranchCommits(FeatureBranch)
  }

  it should "use currently checked out branch if no branch provided and user has no branch stored" in {
    // given
    finder = finderWithEmptyResults
    when(branchCommitsCache.getCheckedOutBranchShortName).thenReturn(BugfixBranch)
    when(branchCommitsCache.getBranchCommits(BugfixBranch)).thenReturn(NoCommitsInBranch)

    // when
    finder.find(Alice.id, branchName = None, Page)
    finder.count(Alice.id, branchName = None)

    // then
    verify(branchCommitsCache, times(2)).getBranchCommits(BugfixBranch)
  }

  private def finderWithEmptyResults = {
    new ToReviewCommitsFinder(config, branchCommitsCache, reviewedCommitsCache, commitsInfoDao, userDao) {
      override def buildToReviewCommitsView(allBranchCommitsToReview: List[String], paging: PagingCriteria[String]) = CommitListView(List.empty, 0, 0)
      override def findToReviewCommitsInBranch(branchCommits: List[BranchCommitCacheEntry], user: User) = List.empty
    }
  }

}