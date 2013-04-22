package com.softwaremill.codebrag.service.github.jgit

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.eclipse.jgit.util.FileUtils
import java.io.File

class InternalGitDirTreeSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter {

  var dirTree: InternalGitDirTree = _

  before {
    dirTree = new InternalGitDirTree
  }

  after {
    deleteRootDirectoryRecursively()
  }

  behavior of "InternalGitDirTree"

  it should "not contain a repository if root directory does not exist" in {
    // given no root directory
    dirTree.containsRepo("someOwner", "someRepo") should be(false)
  }


  it should "not contain a repository if root directory exists but there's no owner directory" in {
    // given
    givenExistingRootDirectory()
    // when
    dirTree.containsRepo("someOwner", "someRepo") should be(false)
  }

  it should "not contain a repository if there's owner directory but no repository directory" in {
    // given
    givenExistingRootDirectory()
    givenExistingRepository("softwaremill", "someOtherProject")
    // when
    dirTree.containsRepo("softwaremill", "codebrag") should be(false)
  }

  it should "contain a repository if its directory exists" in {
    // given
    givenExistingRootDirectory()
    givenExistingRepository("softwaremill", "codebrag")
    // when
    dirTree.containsRepo("softwaremill", "codebrag") should be(true)
  }

  def deleteRootDirectoryRecursively() {
    FileUtils.delete(new File(InternalGitDirTree.Root), FileUtils.RECURSIVE | FileUtils.SKIP_MISSING)
  }

  def givenExistingRootDirectory() {
    FileUtils.mkdirs(new File(InternalGitDirTree.Root))
  }

  def givenExistingRepository(owner: String, repository: String) =
    FileUtils.mkdirs(new File(s"${InternalGitDirTree.Root}/$owner/$repository"))
}
