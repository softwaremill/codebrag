package com.softwaremill.codebrag.repository

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.service.commits.jgit.TemporaryGitRepo
import com.softwaremill.codebrag.repository.config.RepoData
import com.softwaremill.codebrag.domain.MultibranchLoadCommitsResult

class RepositorySpec  extends FlatSpec with ShouldMatchers {

  it should "load commits since given SHA for all branches" in {
    TemporaryGitRepo.withGitRepo { gitRepo =>
      // given
      val repo = new TestRepository(repoData(gitRepo))
      val masterCommits = createCommits(10, gitRepo)
      gitRepo.checkoutBranch("feature")
      val featureCommits = createCommits(5, gitRepo)
      val lastKnownShas = Map(
        "refs/heads/master" -> masterCommits.drop(5).head,
        "refs/heads/feature" -> featureCommits.drop(3).head
      )

      // when
      val loadResult = repo.loadCommitsSince(lastKnownShas)

      // then
      val masterLoadResult = loadResultForBranch("refs/heads/master", loadResult)
      masterLoadResult.commits.map(_.sha) should equal(masterCommits.takeRight(4).reverse)
      val branchLoadResult = loadResultForBranch("refs/heads/feature", loadResult)
      branchLoadResult.commits.map(_.sha) should equal(featureCommits.takeRight(1).reverse)
    }
  }

  it should "load all commits for branches not yet known by Codebrag" in {
    TemporaryGitRepo.withGitRepo { gitRepo =>
    // given
      val repo = new TestRepository(repoData(gitRepo))
      val masterCommits = createCommits(10, gitRepo)
      gitRepo.checkoutBranch("feature")
      val featureCommits = createCommits(5, gitRepo)
      val lastKnownShas = Map(
        "refs/heads/master" -> masterCommits.drop(5).head
      )

      // when
      val loadResult = repo.loadCommitsSince(lastKnownShas)

      // then
      val masterLoadResult = loadResultForBranch("refs/heads/master", loadResult)
      masterLoadResult.commits.map(_.sha) should equal(masterCommits.takeRight(4).reverse)
      val branchLoadResult = loadResultForBranch("refs/heads/feature", loadResult)
      val completeFeatureBranchCommits = masterCommits ++ featureCommits
      branchLoadResult.commits.map(_.sha) should equal(completeFeatureBranchCommits.reverse)
    }
  }

  it should "load nothing for branch when there are no new commits " in {
    TemporaryGitRepo.withGitRepo { gitRepo =>
    // given
      val repo = new TestRepository(repoData(gitRepo))
      val masterCommits = createCommits(10, gitRepo)
      val lastKnownShas = Map(
        "refs/heads/master" -> masterCommits.last
      )

      // when
      val loadResult = repo.loadCommitsSince(lastKnownShas)

      // then
      val masterLoadResult = loadResultForBranch("refs/heads/master", loadResult)
      masterLoadResult.commits.map(_.sha) should be('empty)
    }
  }


  it should "load repository state up to given branch pointers with limit" in {
    val tenthCommit = 9
    
    TemporaryGitRepo.withGitRepo { gitRepo =>
      // given
      val repo = new TestRepository(repoData(gitRepo))
      val masterCommits = createCommits(20, gitRepo)
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
      val masterCommits = createCommits(20, gitRepo)
      gitRepo.checkoutBranch("feature")
      val branchCommits = createCommits(10, gitRepo)
      gitRepo.checkoutBranch("master", create = false)
      val allMasterCommits = masterCommits ++ createCommits(10, gitRepo)
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
      val masterCommits = createCommits(10, gitRepo)
      gitRepo.checkoutBranch("feature")
      createCommits(10, gitRepo)
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
      val masterCommits = createCommits(10, gitRepo)
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

  def loadResultForBranch(branchName: String, loadResult: MultibranchLoadCommitsResult) = {
    loadResult.commits.find(_.branchName == branchName).get
  }

  def shasForBranch(branchName: String, loadResult: MultibranchLoadCommitsResult): List[String] = {
    loadResultForBranch(branchName, loadResult).commits.map(_.sha).toList.reverse
  }

  def createCommits(count: Int, repo: TemporaryGitRepo): List[String] = {
    val shas = for(i <- 1 to count) yield {
      repo.createCommit(s"commit_${i}", (s"file_${i}.txt", s"file_${i}_content"))
    }
    shas.toList
  }

  private def dropCommits(masterCommits: List[String], left: Int, right: Int): List[String] = {
    masterCommits.drop(left).dropRight(right)
  }

  private def repoData(repo: TemporaryGitRepo) = RepoData(repo.tempDir.getAbsolutePath, "temp", "git", None)

  private class TestRepository(val repoData: RepoData) extends Repository {
    protected def pullChangesForRepo() = ???
  }

}
