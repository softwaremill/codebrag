package com.softwaremill.codebrag.repository

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.service.commits.jgit.TemporaryGitRepo
import com.softwaremill.codebrag.repository.config.RepoData
import org.eclipse.jgit.lib.ObjectId

class RepositorySpec  extends FlatSpec with ShouldMatchers {

  it should "get all commits for given branch" in {
    TemporaryGitRepo.withGitRepo { gitRepo =>
      // given
      val sha1 = gitRepo.createCommit("test1", ("test1.txt", "testfile1"))
      val sha2 = gitRepo.createCommit("test2", ("test2.txt", "testfile2"))
      val sha3 = gitRepo.createCommit("test3", ("test3.txt", "testfile3"))

      // when
      val repo = new TestRepository(repoData(gitRepo))
      val commits = repo.getCommitsForBranch("refs/heads/master", None).map(_.getId).map(ObjectId.toString)

      // then
      commits should be(List(sha3, sha2, sha1))
    }
  }

  it should "get empty list of commits when there are no new commits after last known commit" in {
    TemporaryGitRepo.withGitRepo { gitRepo =>
      // given
      val sha1 = gitRepo.createCommit("test1", ("test1.txt", "testfile1"))
      val sha2 = gitRepo.createCommit("test2", ("test2.txt", "testfile2"))

      // when
      val repo = new TestRepository(repoData(gitRepo))
      val commits = repo.getCommitsForBranch("refs/heads/master", Some(sha2)).map(_.getId).map(ObjectId.toString)

      // then
      commits should be('empty)
    }

  }

  it should "get commits for different branches" in {
    TemporaryGitRepo.withGitRepo { gitRepo =>
    // given
      val sha1 = gitRepo.createCommit("test1", ("test1.txt", "testfile1"))
      val sha2 = gitRepo.createCommit("test2", ("test2.txt", "testfile2"))
      gitRepo.checkoutBranch("other_branch")
      val sha3 = gitRepo.createCommit("test3", ("test3.txt", "testfile3"))

      // when
      val repo = new TestRepository(repoData(gitRepo))
      val masterCommits = repo.getCommitsForBranch("refs/heads/master", None).map(_.getId).map(ObjectId.toString)
      val branchCommits = repo.getCommitsForBranch("refs/heads/other_branch", None).map(_.getId).map(ObjectId.toString)

      // then
      masterCommits should be(List(sha2, sha1))
      branchCommits should be(List(sha3, sha2, sha1))
    }

  }

  private def repoData(repo: TemporaryGitRepo) = RepoData(repo.tempDir.getAbsolutePath, "temp", "git", None)

  private class TestRepository(val repoData: RepoData) extends Repository {
    protected def pullChangesForRepo() = ???
  }

}
