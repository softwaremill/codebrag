package com.softwaremill.codebrag.repository

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.service.commits.jgit.TemporaryGitRepo
import com.softwaremill.codebrag.domain.MultibranchLoadCommitsResult

class RepositoryDeltaLoaderSpec  extends FlatSpec with ShouldMatchers {

  private val MaxCommitsForNewBranch = 15

  it should "load commits since given SHA for all branches" in {
    TemporaryGitRepo.withGitRepo { gitRepo =>
    // given
      val repo = gitRepo.repository
      val masterCommits = gitRepo.createCommits(10)
      gitRepo.checkoutBranch("feature")
      val featureCommits = gitRepo.createCommits(5)
      val lastKnownShas = Map(
        "refs/heads/master" -> masterCommits.drop(5).head,
        "refs/heads/feature" -> featureCommits.drop(3).head
      )

      // when
      val loadResult = repo.loadCommitsSince(lastKnownShas, MaxCommitsForNewBranch)

      // then
      val masterLoadResult = loadResultForBranch("refs/heads/master", loadResult)
      masterLoadResult.commits.map(_.sha) should equal(masterCommits.takeRight(4).reverse)
      val branchLoadResult = loadResultForBranch("refs/heads/feature", loadResult)
      branchLoadResult.commits.map(_.sha) should equal(featureCommits.takeRight(1).reverse)
    }
  }

  it should "load max number of last commits for branches not yet known by Codebrag" in {
    TemporaryGitRepo.withGitRepo { gitRepo =>
    // given
      val repo = gitRepo.repository
      val masterCommits = gitRepo.createCommits(10)
      gitRepo.checkoutBranch("feature")
      val featureCommits = gitRepo.createCommits(10)
      val lastKnownShas = Map(
        "refs/heads/master" -> masterCommits.drop(5).head
      )

      // when
      val loadResult = repo.loadCommitsSince(lastKnownShas, MaxCommitsForNewBranch)

      // then
      val masterLoadResult = loadResultForBranch("refs/heads/master", loadResult)
      masterLoadResult.commits.map(_.sha) should equal(masterCommits.takeRight(4).reverse)
      val branchLoadResult = loadResultForBranch("refs/heads/feature", loadResult)
      val completeFeatureBranchCommits = masterCommits ++ featureCommits
      branchLoadResult.commits.map(_.sha) should equal(completeFeatureBranchCommits.takeRight(MaxCommitsForNewBranch).reverse)
    }
  }

  it should "load nothing for branch when there are no new commits " in {
    TemporaryGitRepo.withGitRepo { gitRepo =>
    // given
      val repo = gitRepo.repository
      val masterCommits = gitRepo.createCommits(10)
      val lastKnownShas = Map(
        "refs/heads/master" -> masterCommits.last
      )

      // when
      val loadResult = repo.loadCommitsSince(lastKnownShas, MaxCommitsForNewBranch)

      // then
      val masterLoadResult = loadResultForBranch("refs/heads/master", loadResult)
      masterLoadResult.commits.map(_.sha) should be('empty)
    }
  }

  def loadResultForBranch(branchName: String, loadResult: MultibranchLoadCommitsResult) = {
    loadResult.commits.find(_.branchName == branchName).get
  }

}