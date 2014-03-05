package com.softwaremill.codebrag.repository

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.service.commits.jgit.TemporaryGitRepo

class RepositoryDeltaLoaderSpec  extends FlatSpec with ShouldMatchers with RepositorySpec {

  it should "load commits since given SHA for all branches" in {
    TemporaryGitRepo.withGitRepo { gitRepo =>
    // given
      val repo = new TestRepository(repoData(gitRepo))
      val masterCommits = gitRepo.createCommits(10)
      gitRepo.checkoutBranch("feature")
      val featureCommits = gitRepo.createCommits(5)
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
      val masterCommits = gitRepo.createCommits(10)
      gitRepo.checkoutBranch("feature")
      val featureCommits = gitRepo.createCommits(5)
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
      val masterCommits = gitRepo.createCommits(10)
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

}