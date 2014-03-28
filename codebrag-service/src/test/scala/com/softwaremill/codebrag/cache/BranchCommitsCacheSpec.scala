package com.softwaremill.codebrag.cache

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.MultibranchLoadCommitsResult
import com.softwaremill.codebrag.domain.builder.CommitInfoAssembler
import com.softwaremill.codebrag.domain.CommitsForBranch
import com.softwaremill.codebrag.repository.Repository
import org.mockito.Mockito._
import com.softwaremill.codebrag.service.config.CommitCacheConfig

class BranchCommitsCacheSpec extends FlatSpec with MockitoSugar with BeforeAndAfter with ShouldMatchers {

  var backend: PersistentBackendForCache = _
  var repoCache: BranchCommitsCache = _
  var cacheConfig: CommitCacheConfig = _
  var repository: Repository = _

  val MasterBranch: String = "refs/remotes/origin/master"
  val FeatureBranch: String = "refs/remotes/origin/feature"

  var Commits = List(
    CommitInfoAssembler.randomCommit.withSha("2").get,
    CommitInfoAssembler.randomCommit.withSha("1").get
  )

  var AdditionalCommits = List(
    CommitInfoAssembler.randomCommit.withSha("4").get,
    CommitInfoAssembler.randomCommit.withSha("3").get
  )

  before {
    backend = mock[PersistentBackendForCache]
    cacheConfig = mock[CommitCacheConfig]
    repository = mock[Repository]
    repoCache = new BranchCommitsCache(repository, backend, cacheConfig)

    when(cacheConfig.maxCommitsCachedPerBranch).thenReturn(10)
  }
  
  it should "create branch entry and add commits to cache when no branch exists in cache" in {
    // given
    val masterCommits = List(CommitsForBranch(MasterBranch, Commits, "123abc"))
    val commitsLoaded = MultibranchLoadCommitsResult("codebrag", masterCommits)

    // when
    repoCache.addCommits(commitsLoaded)

    // then
    repoCache.getBranchNames should be(Set(MasterBranch))
    repoCache.getBranchCommits(MasterBranch).map(_.sha) should be(List("2", "1"))
  }

  it should "add commits to the beginning of existing branch in cache" in {
    // given
    val firstCommits = List(CommitsForBranch(MasterBranch, Commits, "123abc"))
    val firstCommitsLoaded = MultibranchLoadCommitsResult("codebrag", firstCommits)
    val nextCommits = List(CommitsForBranch(MasterBranch, AdditionalCommits, "456def"))
    val nextCommitsLoaded = MultibranchLoadCommitsResult("codebrag", nextCommits)

    // when
    repoCache.addCommits(firstCommitsLoaded)
    repoCache.addCommits(nextCommitsLoaded)

    // then
    val expectedShas = List("4", "3", "2", "1")
    repoCache.getBranchCommits(MasterBranch).map(_.sha) should be(expectedShas)
  }
  
  it should "add multiple branches to cache" in {
    // given
    val commits = List(
      CommitsForBranch(MasterBranch, Commits, "123abc"),
      CommitsForBranch(FeatureBranch, AdditionalCommits, "456def")
    )
    val commitsLoaded = MultibranchLoadCommitsResult("codebrag", commits)

    // when
    repoCache.addCommits(commitsLoaded)

    // then
    repoCache.getBranchNames should be(Set(MasterBranch, FeatureBranch))
  }

  it should "get commits for branch or empty list if no such branch exists" in {
    // given
    val masterCommits = List(CommitsForBranch(MasterBranch, Commits, "123abc"))
    val commitsLoaded = MultibranchLoadCommitsResult("codebrag", masterCommits)

    // when
    repoCache.addCommits(commitsLoaded)

    // then
    repoCache.getBranchCommits(MasterBranch) should not be('empty)
    repoCache.getBranchCommits(FeatureBranch) should be('empty)
  }

  it should "initialize cache from repository using previously saved repo state" in {
    // given
    val masterCommits = List(CommitsForBranch(MasterBranch, Commits, "123abc"))
    val commitsLoaded = MultibranchLoadCommitsResult("codebrag", masterCommits)
    when(backend.loadBranchesState()).thenReturn(savedState)
    when(repository.loadLastKnownRepoState(savedState, cacheConfig.maxCommitsCachedPerBranch)).thenReturn(commitsLoaded)

    // when
    repoCache.initialize()

    // then
    repoCache.getBranchCommits(MasterBranch).map(_.sha) should be(List("2", "1"))
  }

  it should "write commits to backend when added to cache" in {
    // given
    val masterCommits = List(CommitsForBranch(MasterBranch, Commits, "123abc"))
    val commitsLoaded = MultibranchLoadCommitsResult("codebrag", masterCommits)

    // when
    repoCache.addCommits(commitsLoaded)

    // then
    verify(backend).persist(commitsLoaded)
  }

  it should "store only certain number of newest commits for given branch" in {
    // given
    val commits = for(i <- (cacheConfig.maxCommitsCachedPerBranch + 10) to 1 by -1) yield {
      CommitInfoAssembler.randomCommit.withSha(i.toString).get
    }
    val masterCommits = List(CommitsForBranch(MasterBranch, commits.toList, "123abc"))
    val commitsLoaded = MultibranchLoadCommitsResult("codebrag", masterCommits)

    // when
    repoCache.addCommits(commitsLoaded)

    // then
    val cachedMasterCommits = repoCache.getBranchCommits(MasterBranch)
    cachedMasterCommits.size should be(cacheConfig.maxCommitsCachedPerBranch)
    val expectedCommitsCached = commits.dropRight(10)
    cachedMasterCommits.map(_.sha) should be(expectedCommitsCached.map(_.sha))
  }

  private def savedState = Map.empty[String, String]

}
