package com.softwaremill.codebrag.service.commits.jgit

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.eclipse.jgit.util.FileUtils
import com.softwaremill.codebrag.service.commits.{GitHubRepoData, TestCodebragConfig}

class InternalGitDirTreeSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter {

  var dirTree: InternalGitDirTree = _

  before {
    dirTree = new InternalGitDirTree(TestCodebragConfig)
  }

  after {
    deleteRootDirectoryRecursively()
  }

  behavior of "InternalGitDirTree"

  it should "not contain a repository if root directory does not exist" in {
    // given no root directory
    dirTree.containsRepo(new GitHubRepoData("someOwner", "someRepo", "token")) should be(false)
  }


  it should "not contain a repository if root directory exists but there's no owner directory" in {
    // given
    givenExistingRootDirectory()
    // when
    dirTree.containsRepo(new GitHubRepoData("someOwner", "someRepo", "token")) should be(false)
  }

  it should "not contain a repository if there's owner directory but no repository directory" in {
    // given
    givenExistingRootDirectory()
    givenExistingRepository("softwaremill", "someOtherProject")
    // when
    dirTree.containsRepo(new GitHubRepoData("softwaremill", "codebrag", "token")) should be(false)
  }

  it should "contain a repository if its directory exists" in {
    // given
    givenExistingRootDirectory()
    givenExistingRepository("softwaremill", "codebrag")
    // when
    dirTree.containsRepo(new GitHubRepoData("softwaremill", "codebrag", "token")) should be(true)
  }

  def deleteRootDirectoryRecursively() {
    FileUtils.delete(dirTree.root.toFile, FileUtils.RECURSIVE | FileUtils.SKIP_MISSING)
  }

  def givenExistingRootDirectory() {
    FileUtils.mkdirs(dirTree.root.toFile)
  }

  def givenExistingRepository(owner: String, repository: String) {
    FileUtils.mkdirs(dirTree.root.resolve(owner).resolve(repository).toFile)
  }
}
