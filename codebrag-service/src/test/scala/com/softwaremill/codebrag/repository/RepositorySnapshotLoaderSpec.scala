package com.softwaremill.codebrag.repository

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.service.commits.jgit.TemporaryGitRepo
import com.softwaremill.codebrag.domain.MultibranchLoadCommitsResult

class RepositorySnapshotLoaderSpec extends FlatSpec with ShouldMatchers with RepositorySpec {

  it should "load repository state up to given branch pointers with limit" in {
    val tenthCommit = 9
    
    TemporaryGitRepo.withGitRepo { gitRepo =>
      // given
      val repo = new TestRepository(repoData(gitRepo))
      val masterCommits = gitRepo.createCommits(20)
      val lastKnownCommits = Map("refs/heads/master" -> masterCommits(tenthCommit))

      // when
      val loadResult = repo.loadLastKnownRepoState(lastKnownCommits, perBranchMaxCommitsCount = 5)

      // then
      val commits = shasForBranch("refs/heads/master", loadResult)
      val expectedCommitsSnapshot = dropCommits(masterCommits, 5, 10)
      commits.length should equal(expectedCommitsSnapshot.length)
      commits should equal(expectedCommitsSnapshot)
    }
  }

  it should "load multiple branches state up to given pointers" in {
    TemporaryGitRepo.withGitRepo { gitRepo =>
    // given
      val repo = new TestRepository(repoData(gitRepo))
      val masterCommits = gitRepo.createCommits(20)
      gitRepo.checkoutBranch("feature")
      val branchCommits = gitRepo.createCommits(10)
      gitRepo.checkoutBranch("master", create = false)
      val allMasterCommits = masterCommits ++ gitRepo.createCommits(10)
      val lastKnownCommits = Map(
        "refs/heads/master" -> allMasterCommits(19), // 20
        "refs/heads/feature" -> branchCommits(7)  // 8
      )

      // when
      val loadResult = repo.loadLastKnownRepoState(lastKnownCommits, perBranchMaxCommitsCount = 5)

      // then
      val loadedMasterCommits = shasForBranch("refs/heads/master", loadResult)
      val expectedMasterCommitsSnapshot = dropCommits(allMasterCommits, left = 15, right = 10)
      loadedMasterCommits.length should equal(expectedMasterCommitsSnapshot.length)
      loadedMasterCommits should equal(expectedMasterCommitsSnapshot)

      val loadedBranchCommits = shasForBranch("refs/heads/feature", loadResult)
      val expectedBranchCommitsSnapshot = dropCommits(branchCommits, left = 3, right = 2)
      loadedBranchCommits.length should equal(expectedBranchCommitsSnapshot.length)
      loadedBranchCommits should equal(expectedBranchCommitsSnapshot)
    }
  }

  it should "not load state of branches that are not yet known by Codebrag" in {
    TemporaryGitRepo.withGitRepo { gitRepo =>
      val repo = new TestRepository(repoData(gitRepo))
      val masterCommits = gitRepo.createCommits(10)
      gitRepo.checkoutBranch("feature")
      gitRepo.createCommits(10)
      val lastKnownCommits = Map(
        "refs/heads/master" -> masterCommits.last
      )

      // when
      val loadResult = repo.loadLastKnownRepoState(lastKnownCommits, perBranchMaxCommitsCount = 5)

      // then
      loadResult.commits.map(_.branchName) should equal(List("refs/heads/master"))
    }
  }

  it should "skip branch that is known to Codebrag but doesn't exist anymore" in {
    TemporaryGitRepo.withGitRepo { gitRepo =>
      val repo = new TestRepository(repoData(gitRepo))
      val masterCommits = gitRepo.createCommits(10)
      val lastKnownCommits = Map(
        "refs/heads/master" -> masterCommits.last,
        "refs/heads/removed" -> "some dummy sha"
      )

      // when
      val loadResult = repo.loadLastKnownRepoState(lastKnownCommits, perBranchMaxCommitsCount = 5)

      // then
      loadResult.commits.map(_.branchName) should equal(List("refs/heads/master"))
    }

  }

  private def shasForBranch(branchName: String, loadResult: MultibranchLoadCommitsResult): List[String] = {
    loadResultForBranch(branchName, loadResult).commits.map(_.sha).toList.reverse
  }

  private def dropCommits(masterCommits: List[String], left: Int, right: Int): List[String] = {
    masterCommits.drop(left).dropRight(right)
  }

}
